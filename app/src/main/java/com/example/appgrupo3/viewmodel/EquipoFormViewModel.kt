package com.example.appgrupo3.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.appgrupo3.model.Equipo
import com.example.appgrupo3.model.TipoEquipo
import com.example.appgrupo3.utils.Formato
import com.example.appgrupo3.utils.ImagenUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.util.UUID

data class EquipoErrores(
    val tag: String? = null,
    val tipo: String? = null,
    val marca: String? = null,
    val serie: String? = null,
    val anio: String? = null,
    val placa: String? = null,
    val contacto: String? = null,
    val ajuste: String? = null,
    val foto: String? = null
) {
    val hayErrores: Boolean get() = listOf(tag, tipo, marca, serie, anio, placa, contacto, ajuste).any { it != null }
}

data class EquipoUiState(
    val id: String? = null,
    val instalacionId: String = "",
    val tag: String = "",
    val tipo: TipoEquipo? = null,
    val marca: String = "",
    val serie: String = "",
    val anio: String = "",
    val placa: String = "",
    val contacto: String = "",
    val ajuste: String = "",
    val fotoRuta: String? = null,
    val errores: EquipoErrores = EquipoErrores(),
    val duplicado: Equipo? = null
) {
    val esNuevo: Boolean get() = id == null
}

class EquipoFormViewModel(
    application: Application,
    private val guardado: SavedStateHandle
) : AndroidViewModel(application) {
    private val _estado = MutableStateFlow(EquipoUiState())
    val estado: StateFlow<EquipoUiState> = _estado.asStateFlow()
    private var iniciado = false
    private var fotoOriginal: String? = null
    private var confirmado = false

    fun iniciar(instalacionId: String, equipo: Equipo?) {
        if (iniciado) return
        iniciado = true
        fotoOriginal = equipo?.fotoPlaca
        _estado.value = if (equipo == null) {
            EquipoUiState(instalacionId = instalacionId)
        } else {
            EquipoUiState(
                id = equipo.id,
                instalacionId = equipo.instalacionId,
                tag = equipo.tag,
                tipo = equipo.tipo,
                marca = equipo.marca,
                serie = equipo.serie,
                anio = equipo.anio.toString(),
                placa = equipo.placa,
                contacto = Formato.aTexto(equipo.contactoMaximo),
                ajuste = Formato.aTexto(equipo.ajusteRele),
                fotoRuta = equipo.fotoPlaca
            )
        }
    }

    fun onTag(v: String) = _estado.update {
        it.copy(tag = v.uppercase().filter { c -> c.isLetterOrDigit() || c in "-/." }.take(20), errores = it.errores.copy(tag = null), duplicado = null)
    }

    fun onTipo(t: TipoEquipo) = _estado.update { it.copy(tipo = t, errores = it.errores.copy(tipo = null, contacto = null, ajuste = null)) }
    fun onMarca(v: String) = _estado.update { it.copy(marca = v.take(40), errores = it.errores.copy(marca = null)) }
    fun onSerie(v: String) = _estado.update { it.copy(serie = v.uppercase().take(30), errores = it.errores.copy(serie = null)) }
    fun onAnio(v: String) = _estado.update { it.copy(anio = v.filter { c -> c.isDigit() }.take(4), errores = it.errores.copy(anio = null)) }
    fun onPlaca(v: String) = _estado.update { it.copy(placa = v.take(80), errores = it.errores.copy(placa = null)) }
    fun onContacto(v: String) = _estado.update { it.copy(contacto = v.take(8), errores = it.errores.copy(contacto = null)) }
    fun onAjuste(v: String) = _estado.update { it.copy(ajuste = v.take(8), errores = it.errores.copy(ajuste = null)) }

    fun onDuplicado(existente: Equipo?) = _estado.update { it.copy(duplicado = existente) }

    fun validar(anioActual: Int = LocalDate.now().year): Boolean {
        val e = _estado.value
        val anio = e.anio.toIntOrNull()
        val contacto = Formato.aNumero(e.contacto)
        val ajuste = Formato.aNumero(e.ajuste)
        val errores = EquipoErrores(
            tag = when {
                e.tag.isBlank() -> "El TAG es obligatorio"
                e.tag.length < 2 -> "El TAG debe tener al menos 2 caracteres"
                else -> null
            },
            tipo = if (e.tipo == null) "Selecciona el tipo de equipo" else null,
            marca = if (e.marca.isBlank()) "La marca es obligatoria" else null,
            serie = if (e.serie.isBlank()) "El número de serie es obligatorio" else null,
            anio = when {
                anio == null -> "Ingresa el año de fabricación"
                anio < 1950 || anio > anioActual -> "El año debe estar entre 1950 y $anioActual"
                else -> null
            },
            placa = if (e.placa.isBlank()) "Copia los datos de placa (potencia, tensiones)" else null,
            contacto = if (e.tipo?.usaContactos == true && (contacto == null || contacto <= 0)) "Ingresa el máximo de contactos de placa en µΩ" else null,
            ajuste = if (e.tipo == TipoEquipo.RELE && (ajuste == null || ajuste <= 0)) "Ingresa el ajuste de corriente en A" else null
        )
        _estado.update { it.copy(errores = errores) }
        return !errores.hayErrores
    }

    fun construir(): Equipo {
        val e = _estado.value
        val tipo = e.tipo ?: TipoEquipo.TRANSFORMADOR
        return Equipo(
            id = e.id ?: "EQ-" + UUID.randomUUID().toString().take(8),
            instalacionId = e.instalacionId,
            tag = e.tag.trim(),
            tipo = tipo,
            marca = e.marca.trim(),
            serie = e.serie.trim(),
            anio = e.anio.toInt(),
            placa = e.placa.trim(),
            contactoMaximo = if (tipo.usaContactos) Formato.aNumero(e.contacto) else null,
            ajusteRele = if (tipo == TipoEquipo.RELE) Formato.aNumero(e.ajuste) else null,
            fotoPlaca = e.fotoRuta
        )
    }

    fun confirmarGuardado() {
        confirmado = true
        val original = fotoOriginal
        if (original != null && original != _estado.value.fotoRuta) ImagenUtils.borrar(original)
    }

    fun prepararFoto(): Uri {
        val (archivo, uri) = ImagenUtils.crearArchivoFoto(getApplication())
        guardado[CLAVE_PENDIENTE] = archivo.absolutePath
        return uri
    }

    fun onFotoTomada(exito: Boolean) {
        val ruta = guardado.get<String>(CLAVE_PENDIENTE)
        guardado.remove<String>(CLAVE_PENDIENTE)
        val archivo = ruta?.let { File(it) } ?: return
        if (exito && archivo.length() > 0) reemplazarFoto(archivo.absolutePath) else archivo.delete()
    }

    fun onCamaraNoDisponible(mensaje: String) {
        ImagenUtils.borrar(guardado.get<String>(CLAVE_PENDIENTE))
        guardado.remove<String>(CLAVE_PENDIENTE)
        mostrarErrorFoto(mensaje)
    }

    fun onImagenGaleria(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            val copia = withContext(Dispatchers.IO) { ImagenUtils.copiarDesdeGaleria(getApplication(), uri) }
            if (copia == null) mostrarErrorFoto("No se pudo leer la imagen seleccionada")
            else reemplazarFoto(copia.absolutePath)
        }
    }

    fun mostrarErrorFoto(mensaje: String) = _estado.update { it.copy(errores = it.errores.copy(foto = mensaje)) }

    private fun reemplazarFoto(nueva: String) {
        val anterior = _estado.value.fotoRuta
        _estado.update { it.copy(fotoRuta = nueva, errores = it.errores.copy(foto = null)) }
        if (anterior != nueva && anterior != fotoOriginal) ImagenUtils.borrar(anterior)
    }

    fun quitarFoto() {
        val anterior = _estado.value.fotoRuta
        _estado.update { it.copy(fotoRuta = null) }
        if (anterior != fotoOriginal) ImagenUtils.borrar(anterior)
    }

    override fun onCleared() {
        val actual = _estado.value.fotoRuta
        if (!confirmado && actual != fotoOriginal) ImagenUtils.borrar(actual)
        ImagenUtils.borrar(guardado.get<String>(CLAVE_PENDIENTE))
        super.onCleared()
    }

    private companion object {
        const val CLAVE_PENDIENTE = "equipo_foto_pendiente"
    }
}
