package com.example.appgrupo3.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object Formato {
    private val simbolos = DecimalFormatSymbols(Locale.forLanguageTag("es-CL"))

    fun numero(valor: Double, decimales: Int = 2): String {
        val patron = if (decimales == 0) "#,##0" else "#,##0." + "#".repeat(decimales)
        return DecimalFormat(patron, simbolos).format(valor)
    }

    fun aTexto(valor: Double?): String {
        if (valor == null) return ""
        val texto = if (valor == valor.toLong().toDouble()) valor.toLong().toString() else valor.toString()
        return texto.replace('.', ',')
    }

    fun aNumero(texto: String): Double? = texto.trim().replace(".", "").replace(',', '.').toDoubleOrNull()

    fun porcentaje(valor: Double): String = (if (valor > 0) "+" else "") + numero(valor, 1) + " %"
}
