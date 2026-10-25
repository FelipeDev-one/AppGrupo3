package com.example.appgrupo3.model

enum class Perfil(val etiqueta: String) {
    TECNICO("Técnico"),
    INGENIERO("Ingeniero de pruebas"),
    JEFE("Jefe técnico"),
    CLIENTE("Usuario cliente");

    val esInterno: Boolean get() = this != CLIENTE
}

enum class Categoria(val etiqueta: String) {
    PRIMARIO("Primario"),
    SECUNDARIO("Secundario")
}

enum class TipoEquipo(val etiqueta: String, val categoria: Categoria) {
    TRANSFORMADOR("Transformador", Categoria.PRIMARIO),
    INTERRUPTOR("Interruptor", Categoria.PRIMARIO),
    CELDA_MT("Celda MT", Categoria.PRIMARIO),
    CABLE("Cable de poder", Categoria.PRIMARIO),
    RELE("Relé de protección", Categoria.SECUNDARIO);

    val usaContactos: Boolean get() = this == INTERRUPTOR || this == CELDA_MT
}

enum class TipoPrueba(val etiqueta: String) {
    AISLACION("Resistencia de aislación"),
    TTR("Relación de transformación (TTR)"),
    DEVANADOS("Resistencia de devanados"),
    CONTACTOS("Resistencia de contactos"),
    OPERACION_RELE("Prueba de operación (inyección secundaria)")
}

enum class TipoInstrumento(val etiqueta: String, val pruebas: Set<TipoPrueba>) {
    MEGOHMETRO("Megóhmetro", setOf(TipoPrueba.AISLACION)),
    TTR("Medidor de relación", setOf(TipoPrueba.TTR)),
    MICROOHMIMETRO("Micro-ohmímetro", setOf(TipoPrueba.CONTACTOS, TipoPrueba.DEVANADOS)),
    INYECCION("Equipo de inyección secundaria", setOf(TipoPrueba.OPERACION_RELE))
}

enum class Resultado(val etiqueta: String, val gravedad: Int) {
    APROBADO("Aprobado", 0),
    OBSERVADO("Observado", 1),
    RECHAZADO("Rechazado", 2)
}

enum class EstadoProtocolo(val etiqueta: String) {
    BORRADOR("Borrador"),
    REGISTRADO("Registrado en terreno"),
    OBSERVADO("Observado"),
    FORMALIZADO("Formalizado"),
    ANULADO("Anulado")
}

enum class TipoVisita(val etiqueta: String) {
    SOLO_PRUEBAS("Solo pruebas"),
    PRUEBAS_Y_MANTENIMIENTO("Pruebas y mantenimiento")
}

enum class UnidadAislacion(val etiqueta: String, val factor: Double) {
    MEGA("MΩ", 1.0),
    GIGA("GΩ", 1000.0)
}

enum class EstadoCalibracion(val etiqueta: String) {
    VIGENTE("Vigente"),
    POR_VENCER("Vence en 30 días o menos"),
    VENCIDO("Calibración vencida")
}
