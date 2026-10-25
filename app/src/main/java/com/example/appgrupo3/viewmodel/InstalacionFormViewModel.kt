package com.example.appgrupo3.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appgrupo3.model.Instalacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

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
            errorNombre = Validaciones.errorNombre(e.nombre, "El nombre"),
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
