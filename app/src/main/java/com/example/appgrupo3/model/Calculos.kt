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

    fun errorTtrMaximo(valores: Map<String, Double>): Double? {
        val teorica = valores["teorica"] ?: return null
        if (teorica <= 0) return null
        val medidas = Catalogo.fasesTtr.map { valores[it] ?: return null }
        return medidas.maxOf { errorTtr(teorica, it) }
    }

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

    fun faltantes(prueba: Prueba, equipo: Equipo): List<String> {
        if (!prueba.realizada) {
            return if (prueba.motivoNoRealizada.isBlank()) listOf("motivo de no realización") else emptyList()
        }
        val lista = mutableListOf<String>()
        if (prueba.instrumentoSerie == null) lista += "instrumento"
        if (Catalogo.usaTension(prueba.tipo) && prueba.tensionKv == null) lista += "tensión de prueba"
        Catalogo.campos(prueba.tipo, equipo.tipo).forEach { campo ->
            val omitible = campo.opcional && prueba.noOpera
            if (!omitible && prueba.valores[campo.clave] == null) lista += campo.etiqueta
        }
        if (prueba.tipo == TipoPrueba.CONTACTOS && equipo.contactoMaximo == null) lista += "máximo de placa del equipo"
        return lista
    }

    fun estaCompleta(prueba: Prueba, equipo: Equipo): Boolean = faltantes(prueba, equipo).isEmpty()

    fun resultado(prueba: Prueba, equipo: Equipo): Resultado? {
        if (!prueba.realizada) return null
        return resultadoDeValores(prueba.tipo, prueba.valores, prueba.noOpera, prueba.resultadoManual, equipo)
    }

    fun resultadoDeValores(
        tipo: TipoPrueba,
        valores: Map<String, Double>,
        noOpera: Boolean,
        manual: Resultado?,
        equipo: Equipo
    ): Resultado? = when (tipo) {
        TipoPrueba.AISLACION -> resultadoAislacion(valores, equipo.tipo)
        TipoPrueba.TTR -> errorTtrMaximo(valores)?.let { clasificarErrorTtr(it) }
        TipoPrueba.DEVANADOS -> manual
        TipoPrueba.CONTACTOS -> {
            val maximo = equipo.contactoMaximo
            val polos = (1..3).map { valores["P$it"] }
            if (maximo == null || maximo <= 0 || polos.any { it == null }) null
            else clasificarContactos(porcentajeContactos(polos.maxOf { it!! }, maximo))
        }
        TipoPrueba.OPERACION_RELE -> {
            val esperado = valores["t_esperado"]
            val medido = valores["t_medido"]
            when {
                noOpera -> Resultado.RECHAZADO
                esperado == null || medido == null || esperado <= 0 -> null
                else -> clasificarRele(diferenciaRele(esperado, medido))
            }
        }
    }

    private fun resultadoAislacion(valores: Map<String, Double>, tipo: TipoEquipo): Resultado? {
        val pares = Catalogo.paresAislacion(tipo)
        val resultados = pares.map { par ->
            val uno = valores[par.claveUnMinuto] ?: return null
            if (uno < LECTURA_MINIMA_MOHM) return@map Resultado.RECHAZADO
            val claveDiez = par.claveDiezMinutos ?: return@map Resultado.APROBADO
            val diez = valores[claveDiez] ?: return null
            clasificarIp(indicePolarizacion(uno, diez))
        }
        return peor(resultados)
    }

    fun detalle(prueba: Prueba, equipo: Equipo): List<String> {
        if (!prueba.realizada) return emptyList()
        val v = prueba.valores
        return when (prueba.tipo) {
            TipoPrueba.AISLACION -> Catalogo.paresAislacion(equipo.tipo).mapNotNull { par ->
                val uno = v[par.claveUnMinuto] ?: return@mapNotNull null
                val diez = par.claveDiezMinutos?.let { v[it] }
                if (diez == null) "${par.nombre}: ${Formato.numero(uno)} MΩ a 1 min"
                else "${par.nombre}: IP ${Formato.numero(indicePolarizacion(uno, diez), 2)}"
            }
            TipoPrueba.TTR -> errorTtrMaximo(v)?.let { listOf("Error máximo ${Formato.numero(it, 2)} %") } ?: emptyList()
            TipoPrueba.CONTACTOS -> {
                val maximo = equipo.contactoMaximo
                val polos = (1..3).mapNotNull { v["P$it"] }
                if (maximo != null && polos.size == 3) {
                    listOf("Mayor polo ${Formato.numero(porcentajeContactos(polos.max(), maximo), 1)} % del máximo de placa (${Formato.numero(maximo)} µΩ)")
                } else emptyList()
            }
            TipoPrueba.OPERACION_RELE -> {
                val e = v["t_esperado"]
                val m = v["t_medido"]
                when {
                    prueba.noOpera -> listOf("El relé no operó")
                    e != null && m != null && e > 0 -> listOf("Diferencia de tiempo ${Formato.numero(diferenciaRele(e, m), 1)} %")
                    else -> emptyList()
                }
            }
            TipoPrueba.DEVANADOS -> if (prueba.resultadoManual == null) listOf("Resultado pendiente del ingeniero (RN-08c)") else emptyList()
        }
    }

    fun global(protocolo: Protocolo, equipo: Equipo): Resultado? =
        peor(protocolo.pruebas.mapNotNull { resultado(it, equipo) })

    fun comparar(actual: Protocolo, anterior: Protocolo?, equipo: Equipo): List<ComparacionPrueba> {
        if (anterior == null) return emptyList()
        return actual.pruebas.mapNotNull { prueba ->
            val previa = anterior.prueba(prueba.tipo) ?: return@mapNotNull null
            if (!prueba.realizada || !previa.realizada) return@mapNotNull null
            val variaciones = Catalogo.campos(prueba.tipo, equipo.tipo)
                .filter { it.principal }
                .mapNotNull { campo ->
                    val a = previa.valores[campo.clave]
                    val b = prueba.valores[campo.clave]
                    if (a == null || b == null) null else Variacion(campo, a, b)
                }
            if (variaciones.isEmpty()) null else ComparacionPrueba(prueba.tipo, anterior.numero, variaciones)
        }
    }
}
