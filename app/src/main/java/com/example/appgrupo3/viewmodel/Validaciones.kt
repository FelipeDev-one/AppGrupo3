package com.example.appgrupo3.viewmodel

import com.example.appgrupo3.utils.RutValidator

object Validaciones {
    val patronCorreo = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun limpiarRut(valor: String): String =
        valor.filter { it.isDigit() || it == 'k' || it == 'K' || it == '.' || it == '-' }.take(12)

    fun errorRut(rut: String): String? = when {
        rut.isBlank() -> "El RUT es obligatorio"
        !RutValidator.esValido(rut) -> "RUT inválido, revisa el dígito verificador (RN-03)"
        else -> null
    }

    fun errorNombre(nombre: String, campo: String): String? = when {
        nombre.isBlank() -> "Ingresa ${campo.lowercase()}"
        nombre.trim().length < 3 -> "$campo debe tener al menos 3 caracteres"
        else -> null
    }
}
