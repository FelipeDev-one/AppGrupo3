package com.example.appgrupo3.model

data class Campo(
    val clave: String,
    val etiqueta: String,
    val unidad: String,
    val principal: Boolean = true,
    val esAislacion: Boolean = false,
    val opcional: Boolean = false
)

data class ParAislacion(val nombre: String, val claveUnMinuto: String, val claveDiezMinutos: String?)

object Catalogo {
    private val devanados = listOf("AT-BT", "AT-T", "BT-T")
    private val fases = listOf("R", "S", "T")

    fun pruebasDe(tipo: TipoEquipo): List<TipoPrueba> = when (tipo) {
        TipoEquipo.TRANSFORMADOR -> listOf(TipoPrueba.AISLACION, TipoPrueba.TTR, TipoPrueba.DEVANADOS)
        TipoEquipo.INTERRUPTOR, TipoEquipo.CELDA_MT -> listOf(TipoPrueba.AISLACION, TipoPrueba.CONTACTOS)
        TipoEquipo.CABLE -> listOf(TipoPrueba.AISLACION)
        TipoEquipo.RELE -> listOf(TipoPrueba.OPERACION_RELE)
    }

    fun paresAislacion(tipo: TipoEquipo): List<ParAislacion> = when (tipo) {
        TipoEquipo.TRANSFORMADOR -> devanados.map { ParAislacion(it, "${it}_1", "${it}_10") }
        TipoEquipo.CABLE -> fases.map { ParAislacion("Fase $it", "${it}_1", "${it}_10") }
        else -> listOf(ParAislacion("Aislación", "L_1", null))
    }

    fun campos(prueba: TipoPrueba, equipo: TipoEquipo): List<Campo> = when (prueba) {
        TipoPrueba.AISLACION -> paresAislacion(equipo).flatMap { par ->
            listOfNotNull(
                Campo(par.claveUnMinuto, "${par.nombre} · 1 min", "MΩ", principal = true, esAislacion = true),
                par.claveDiezMinutos?.let { Campo(it, "${par.nombre} · 10 min", "MΩ", principal = false, esAislacion = true) }
            )
        }
        TipoPrueba.TTR -> listOf(
            Campo("teorica", "Relación teórica", ""),
            Campo("medida", "Relación medida", ""),
            Campo("tap", "Posición del cambiador", "", principal = false)
        )
        TipoPrueba.DEVANADOS -> fases.map { Campo(it, "Fase $it", "mΩ") }
        TipoPrueba.CONTACTOS -> (1..3).map { Campo("P$it", "Polo $it", "µΩ") }
        TipoPrueba.OPERACION_RELE -> listOf(
            Campo("ajuste", "Ajuste de corriente", "A", principal = false),
            Campo("corriente", "Corriente medida de operación", "A", opcional = true),
            Campo("t_esperado", "Tiempo esperado", "ms", principal = false),
            Campo("t_medido", "Tiempo medido", "ms", opcional = true)
        )
    }

    fun usaTension(prueba: TipoPrueba): Boolean = prueba == TipoPrueba.AISLACION

    fun pruebasIniciales(equipo: Equipo): List<Prueba> = pruebasDe(equipo.tipo).map { tipo ->
        if (tipo == TipoPrueba.OPERACION_RELE && equipo.ajusteRele != null) {
            Prueba(tipo, valores = mapOf("ajuste" to equipo.ajusteRele))
        } else {
            Prueba(tipo)
        }
    }
}
