package com.example.appgrupo3.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appgrupo3.model.Recepcion
import com.example.appgrupo3.model.Usuario
import com.example.appgrupo3.utils.RutValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
    fun onRut(v: String) = _estado.update { it.copy(rut = Validaciones.limpiarRut(v), errorRut = null) }
    fun onCargo(v: String) = _estado.update { it.copy(cargo = v.take(50), errorCargo = null) }

    fun sugerir(usuario: Usuario) = _estado.update {
        it.copy(nombre = usuario.nombre, rut = usuario.rut, cargo = usuario.cargo, errorNombre = null, errorRut = null, errorCargo = null)
    }

    fun validar(): Boolean {
        val e = _estado.value
        val nuevo = e.copy(
            errorNombre = Validaciones.errorNombre(e.nombre, "El nombre"),
            errorRut = Validaciones.errorRut(e.rut),
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
