package com.example.appgrupo3.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appgrupo3.model.Cliente
import com.example.appgrupo3.utils.RutValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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

    fun onRut(v: String) = _estado.update { it.copy(rut = Validaciones.limpiarRut(v), errorRut = null) }
    fun onRazon(v: String) = _estado.update { it.copy(razonSocial = v.take(60), errorRazon = null) }
    fun onContacto(v: String) = _estado.update { it.copy(contacto = v.take(50), errorContacto = null) }
    fun onCorreo(v: String) = _estado.update { it.copy(correo = v.trim().take(60), errorCorreo = null) }

    fun validar(): Boolean {
        val e = _estado.value
        val nuevo = e.copy(
            errorRut = Validaciones.errorRut(e.rut),
            errorRazon = Validaciones.errorNombre(e.razonSocial, "La razón social"),
            errorContacto = Validaciones.errorNombre(e.contacto, "El contacto"),
            errorCorreo = when {
                e.correo.isBlank() -> "El correo es obligatorio"
                !Validaciones.patronCorreo.matches(e.correo) -> "Correo con formato inválido"
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
