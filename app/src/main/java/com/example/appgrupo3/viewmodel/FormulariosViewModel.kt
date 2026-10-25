package com.example.appgrupo3.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appgrupo3.model.Cliente
import com.example.appgrupo3.model.Instalacion
import com.example.appgrupo3.model.Instrumento
import com.example.appgrupo3.model.Perfil
import com.example.appgrupo3.model.Recepcion
import com.example.appgrupo3.model.TipoInstrumento
import com.example.appgrupo3.model.Usuario
import com.example.appgrupo3.utils.RutValidator
import com.example.appgrupo3.utils.Tiempo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

private val patronCorreo = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

private fun limpiarRut(valor: String): String =
    valor.filter { it.isDigit() || it == 'k' || it == 'K' || it == '.' || it == '-' }.take(12)

private fun errorRut(rut: String): String? = when {
    rut.isBlank() -> "El RUT es obligatorio"
    !RutValidator.esValido(rut) -> "RUT inválido, revisa el dígito verificador (RN-03)"
    else -> null
}

private fun errorNombre(nombre: String, campo: String): String? = when {
    nombre.isBlank() -> "$campo es obligatorio"
    nombre.trim().length < 3 -> "$campo debe tener al menos 3 caracteres"
    else -> null
}

data class ClienteUiState(
    val esNuevo: Boolean = true,
    val rut: String = "",
    val razonSocial: String = "",
    val contacto: String = "",
    val correo: String = "",
    val errorRut: String? = null,
    val errorRazon: String? = null,
    val errorContacto: String? = null,
    val errorCorreo: String? = null
)

class ClienteFormViewModel : ViewModel() {
    private val _estado = MutableStateFlow(ClienteUiState())
    val estado: StateFlow<ClienteUiState> = _estado.asStateFlow()
    private var iniciado = false

    fun iniciar(cliente: Cliente?) {
        if (iniciado) return
        iniciado = true
        if (cliente != null) {
            _estado.value = ClienteUiState(false, cliente.rut, cliente.razonSocial, cliente.contacto, cliente.correo)
        }
    }

    fun onRut(v: String) = _estado.update { it.copy(rut = limpiarRut(v), errorRut = null) }
    fun onRazon(v: String) = _estado.update { it.copy(razonSocial = v.take(60), errorRazon = null) }
    fun onContacto(v: String) = _estado.update { it.copy(contacto = v.take(50), errorContacto = null) }
    fun onCorreo(v: String) = _estado.update { it.copy(correo = v.trim().take(60), errorCorreo = null) }

    fun validar(): Boolean {
        val e = _estado.value
        val nuevo = e.copy(
            errorRut = errorRut(e.rut),
            errorRazon = errorNombre(e.razonSocial, "La razón social"),
            errorContacto = errorNombre(e.contacto, "El contacto"),
            errorCorreo = when {
                e.correo.isBlank() -> "El correo es obligatorio"
                !patronCorreo.matches(e.correo) -> "Correo con formato inválido"
                else -> null
            }
        )
        _estado.value = nuevo
        return listOf(nuevo.errorRut, nuevo.errorRazon, nuevo.errorContacto, nuevo.errorCorreo).all { it == null }
    }

    fun construir(): Cliente {
        val e = _estado.value
        return Cliente(RutValidator.formatear(e.rut), e.razonSocial.trim(), e.contacto.trim(), e.correo.trim())
    }
}

data class InstalacionUiState(
    val id: String? = null,
    val clienteRut: String = "",
    val nombre: String = "",
    val comuna: String = "",
    val sinSenal: Boolean = false,
    val errorNombre: String? = null,
    val errorComuna: String? = null
)

class InstalacionFormViewModel : ViewModel() {
    private val _estado = MutableStateFlow(InstalacionUiState())
    val estado: StateFlow<InstalacionUiState> = _estado.asStateFlow()
    private var iniciado = false

    fun iniciar(clienteRut: String, instalacion: Instalacion?) {
        if (iniciado) return
        iniciado = true
        _estado.value = if (instalacion == null) InstalacionUiState(clienteRut = clienteRut)
        else InstalacionUiState(instalacion.id, instalacion.clienteRut, instalacion.nombre, instalacion.comuna, instalacion.sinSenal)
    }

    fun onNombre(v: String) = _estado.update { it.copy(nombre = v.take(50), errorNombre = null) }
    fun onComuna(v: String) = _estado.update { it.copy(comuna = v.take(40), errorComuna = null) }
    fun onSinSenal(v: Boolean) = _estado.update { it.copy(sinSenal = v) }

    fun validar(): Boolean {
        val e = _estado.value
        val nuevo = e.copy(
            errorNombre = errorNombre(e.nombre, "El nombre"),
            errorComuna = if (e.comuna.isBlank()) "La comuna es obligatoria" else null
        )
        _estado.value = nuevo
        return nuevo.errorNombre == null && nuevo.errorComuna == null
    }

    fun construir(): Instalacion {
        val e = _estado.value
        return Instalacion(e.id ?: "INS-" + UUID.randomUUID().toString().take(8), e.clienteRut, e.nombre.trim(), e.comuna.trim(), e.sinSenal)
    }
}

data class RecepcionUiState(
    val nombre: String = "",
    val rut: String = "",
    val cargo: String = "",
    val errorNombre: String? = null,
    val errorRut: String? = null,
    val errorCargo: String? = null
)

class RecepcionViewModel : ViewModel() {
    private val _estado = MutableStateFlow(RecepcionUiState())
    val estado: StateFlow<RecepcionUiState> = _estado.asStateFlow()

    fun onNombre(v: String) = _estado.update { it.copy(nombre = v.take(60), errorNombre = null) }
    fun onRut(v: String) = _estado.update { it.copy(rut = limpiarRut(v), errorRut = null) }
    fun onCargo(v: String) = _estado.update { it.copy(cargo = v.take(50), errorCargo = null) }

    fun sugerir(usuario: Usuario) = _estado.update {
        it.copy(nombre = usuario.nombre, rut = usuario.rut, cargo = usuario.cargo, errorNombre = null, errorRut = null, errorCargo = null)
    }

    fun validar(): Boolean {
        val e = _estado.value
        val nuevo = e.copy(
            errorNombre = errorNombre(e.nombre, "El nombre"),
            errorRut = errorRut(e.rut),
            errorCargo = if (e.cargo.isBlank()) "El cargo es obligatorio" else null
        )
        _estado.value = nuevo
        return listOf(nuevo.errorNombre, nuevo.errorRut, nuevo.errorCargo).all { it == null }
    }

    fun construir(ahora: Long): Recepcion {
        val e = _estado.value
        return Recepcion(e.nombre.trim(), RutValidator.formatear(e.rut), e.cargo.trim(), ahora)
    }
}

data class InstrumentoUiState(
    val esNuevo: Boolean = true,
    val serie: String = "",
    val tipo: TipoInstrumento? = null,
    val nombre: String = "",
    val modelo: String = "",
    val calibracion: String = "",
    val vencimiento: String = "",
    val errorSerie: String? = null,
    val errorTipo: String? = null,
    val errorNombre: String? = null,
    val errorModelo: String? = null,
    val errorCalibracion: String? = null,
    val errorVencimiento: String? = null
)

class InstrumentoFormViewModel : ViewModel() {
    private val _estado = MutableStateFlow(InstrumentoUiState())
    val estado: StateFlow<InstrumentoUiState> = _estado.asStateFlow()
    private var iniciado = false

    fun iniciar(instrumento: Instrumento?) {
        if (iniciado) return
        iniciado = true
        if (instrumento != null) {
            _estado.value = InstrumentoUiState(
                esNuevo = false,
                serie = instrumento.serie,
                tipo = instrumento.tipo,
                nombre = instrumento.nombre,
                modelo = instrumento.modelo,
                calibracion = Tiempo.fechaLegible(instrumento.calibracion),
                vencimiento = Tiempo.fechaLegible(instrumento.vencimiento)
            )
        }
    }

    private fun fecha(v: String) = v.filter { it.isDigit() || it == '-' }.take(10)

    fun onSerie(v: String) = _estado.update { it.copy(serie = v.uppercase().take(20), errorSerie = null) }
    fun onTipo(t: TipoInstrumento) = _estado.update { it.copy(tipo = t, errorTipo = null) }
    fun onNombre(v: String) = _estado.update { it.copy(nombre = v.take(40), errorNombre = null) }
    fun onModelo(v: String) = _estado.update { it.copy(modelo = v.take(40), errorModelo = null) }
    fun onCalibracion(v: String) = _estado.update { it.copy(calibracion = fecha(v), errorCalibracion = null) }
    fun onVencimiento(v: String) = _estado.update { it.copy(vencimiento = fecha(v), errorVencimiento = null) }

    fun validar(): Boolean {
        val e = _estado.value
        val cal = Tiempo.desdeLegible(e.calibracion)
        val ven = Tiempo.desdeLegible(e.vencimiento)
        val nuevo = e.copy(
            errorSerie = if (e.serie.length < 4) "Ingresa el número de serie (mínimo 4 caracteres)" else null,
            errorTipo = if (e.tipo == null) "Selecciona el tipo de instrumento" else null,
            errorNombre = errorNombre(e.nombre, "El nombre"),
            errorModelo = if (e.modelo.isBlank()) "Ingresa marca y modelo" else null,
            errorCalibracion = if (cal == null) "Usa el formato dd-mm-aaaa" else null,
            errorVencimiento = when {
                ven == null -> "Usa el formato dd-mm-aaaa"
                cal != null && !ven.isAfter(cal) -> "Debe ser posterior a la calibración"
                else -> null
            }
        )
        _estado.value = nuevo
        return listOf(nuevo.errorSerie, nuevo.errorTipo, nuevo.errorNombre, nuevo.errorModelo, nuevo.errorCalibracion, nuevo.errorVencimiento)
            .all { it == null }
    }

    fun construir(): Instrumento {
        val e = _estado.value
        return Instrumento(
            serie = e.serie.trim(),
            tipo = e.tipo ?: TipoInstrumento.MEGOHMETRO,
            nombre = e.nombre.trim(),
            modelo = e.modelo.trim(),
            calibracion = Tiempo.desdeLegible(e.calibracion).toString(),
            vencimiento = Tiempo.desdeLegible(e.vencimiento).toString()
        )
    }
}

data class PersonalUiState(
    val rut: String = "",
    val nombre: String = "",
    val perfil: Perfil = Perfil.TECNICO,
    val errorRut: String? = null,
    val errorNombre: String? = null
)

class PersonalFormViewModel : ViewModel() {
    private val _estado = MutableStateFlow(PersonalUiState())
    val estado: StateFlow<PersonalUiState> = _estado.asStateFlow()

    fun onRut(v: String) = _estado.update { it.copy(rut = limpiarRut(v), errorRut = null) }
    fun onNombre(v: String) = _estado.update { it.copy(nombre = v.take(60), errorNombre = null) }
    fun onPerfil(p: Perfil) = _estado.update { it.copy(perfil = p) }

    fun validar(): Boolean {
        val e = _estado.value
        val nuevo = e.copy(
            errorRut = errorRut(e.rut),
            errorNombre = when {
                e.nombre.isBlank() -> "El nombre es obligatorio"
                e.nombre.trim().split(" ").filter { it.isNotBlank() }.size < 2 -> "Ingresa nombre y apellido"
                else -> null
            }
        )
        _estado.value = nuevo
        return nuevo.errorRut == null && nuevo.errorNombre == null
    }

    fun construir(): Usuario {
        val e = _estado.value
        return Usuario(RutValidator.formatear(e.rut), e.nombre.trim(), e.perfil)
    }
}
