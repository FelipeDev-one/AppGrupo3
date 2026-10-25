package com.example.appgrupo3.model

import com.example.appgrupo3.utils.Formato
import kotlin.math.abs

data class Variacion(
    val campo: Campo,
    val anterior: Double,
    val actual: Double
) {
    val porcentaje: Double get() = if (anterior == 0.0) 0.0 else (actual - anterior) / anterior * 100
    val significativa: Boolean get() = abs(porcentaje) > Calculos.LIMITE_VARIACION
}

data class ComparacionPrueba(
    val tipo: TipoPrueba,
    val protocoloAnterior: String,
    val variaciones: List<Variacion>
) {
    val significativa: Boolean get() = variaciones.any { it.significativa }
}

object Calculos {
    const val LECTURA_MINIMA_MOHM = 100.0
    const val LIMITE_VARIACION = 25.0

    fun indicePolarizacion(unMinuto: Double, diezMinutos: Double): Double = diezMinutos / unMinuto

    fun clasificarIp(ip: Double): Resultado = when {
        ip >= 2.0 -> Resultado.APROBADO
        ip >= 1.0 -> Resultado.OBSERVADO
        else -> Resultado.RECHAZADO
    }

    fun errorTtr(teorica: Double, medida: Double): Double = abs(medida - teorica) / teorica * 100

    fun clasificarErrorTtr(error: Double): Resultado = when {
        error <= 0.5 -> Resultado.APROBADO
        error <= 1.0 -> Resultado.OBSERVADO
        else -> Resultado.RECHAZADO
    }

    fun porcentajeContactos(medido: Double, maximo: Double): Double = medido / maximo * 100

    fun clasificarContactos(porcentaje: Double): Resultado = when {
        porcentaje <= 100.0 -> Resultado.APROBADO
        porcentaje <= 120.0 -> Resultado.OBSERVADO
        else -> Resultado.RECHAZADO
    }

    fun diferenciaRele(esperado: Double, medido: Double): Double = abs(medido - esperado) / esperado * 100

    fun clasificarRele(diferencia: Double): Resultado = when {
        diferencia <= 5.0 -> Resultado.APROBADO
        diferencia <= 10.0 -> Resultado.OBSERVADO
        else -> Resultado.RECHAZADO
    }

    fun peor(resultados: List<Resultado>): Resultado? = resultados.maxByOrNull { it.gravedad }
}
