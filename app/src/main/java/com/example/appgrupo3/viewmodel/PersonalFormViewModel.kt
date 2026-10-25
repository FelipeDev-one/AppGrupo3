package com.example.appgrupo3.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appgrupo3.model.Perfil
import com.example.appgrupo3.model.Usuario
import com.example.appgrupo3.utils.RutValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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

    fun onRut(v: String) = _estado.update { it.copy(rut = Validaciones.limpiarRut(v), errorRut = null) }
    fun onNombre(v: String) = _estado.update { it.copy(nombre = v.take(60), errorNombre = null) }
    fun onPerfil(p: Perfil) = _estado.update { it.copy(perfil = p) }

    fun validar(): Boolean {
        val e = _estado.value
        val nuevo = e.copy(
            errorRut = Validaciones.errorRut(e.rut),
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
