package com.example.appgrupo3.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appgrupo3.model.Sistema
import com.example.appgrupo3.model.Usuario
import com.example.appgrupo3.utils.RutValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val rut: String = "",
    val error: String? = null
)

class LoginViewModel : ViewModel() {
    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    fun onRutChange(valor: String) {
        val limpio = valor.filter { it.isDigit() || it == 'k' || it == 'K' || it == '.' || it == '-' }.take(12)
        _estado.update { it.copy(rut = limpio, error = null) }
    }

    fun elegir(usuario: Usuario) {
        _estado.update { it.copy(rut = usuario.rut, error = null) }
    }

    fun validar(sistema: Sistema): Usuario? {
        val rut = _estado.value.rut.trim()
        val error = when {
            rut.isBlank() -> "Ingresa tu RUT"
            !RutValidator.esValido(rut) -> "El RUT no es válido, revisa el dígito verificador (RN-03)"
            else -> null
        }
        if (error != null) {
            _estado.update { it.copy(error = error) }
            return null
        }
        val usuario = sistema.usuario(RutValidator.formatear(rut))
        val errorUsuario = when {
            usuario == null -> "Este RUT no tiene una cuenta habilitada"
            !usuario.activo -> "Tu cuenta está desactivada, habla con el jefe técnico"
            else -> null
        }
        _estado.update { it.copy(error = errorUsuario) }
        return if (errorUsuario == null) usuario else null
    }
}
