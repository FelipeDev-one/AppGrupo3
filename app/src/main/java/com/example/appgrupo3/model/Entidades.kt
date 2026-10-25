package com.example.appgrupo3.model

data class Cliente(
    val rut: String,
    val razonSocial: String,
    val contacto: String,
    val correo: String
)

data class Instalacion(
    val id: String,
    val clienteRut: String,
    val nombre: String,
    val comuna: String,
    val sinSenal: Boolean = false
)

data class Equipo(
    val id: String,
    val instalacionId: String,
    val tag: String,
    val tipo: TipoEquipo,
    val marca: String,
    val serie: String,
    val anio: Int,
    val placa: String,
    val contactoMaximo: Double? = null,
    val ajusteRele: Double? = null,
    val fotoPlaca: String? = null
)

data class Usuario(
    val rut: String,
    val nombre: String,
    val perfil: Perfil,
    val cargo: String = "",
    val clienteRut: String? = null,
    val activo: Boolean = true
)

data class Instrumento(
    val serie: String,
    val tipo: TipoInstrumento,
    val nombre: String,
    val modelo: String,
    val calibracion: String,
    val vencimiento: String
)

data class Recepcion(
    val nombre: String,
    val rut: String,
    val cargo: String,
    val fechaHora: Long
)

data class Visita(
    val numero: String,
    val clienteRut: String,
    val instalacionId: String,
    val fecha: String,
    val tipo: TipoVisita,
    val tecnicoRut: String,
    val ingenieroRut: String,
    val recepcion: Recepcion? = null
) {
    val cerrada: Boolean get() = recepcion != null
}

data class Prueba(
    val tipo: TipoPrueba,
    val realizada: Boolean = true,
    val motivoNoRealizada: String = "",
    val instrumentoSerie: String? = null,
    val tensionKv: Double? = null,
    val unidad: UnidadAislacion = UnidadAislacion.MEGA,
    val valores: Map<String, Double> = emptyMap(),
    val noOpera: Boolean = false,
    val resultadoManual: Resultado? = null,
    val valoresPrevios: Map<String, Double>? = null,
    val resultadoPrevio: Resultado? = null
)

data class CambioEstado(
    val desde: EstadoProtocolo?,
    val hacia: EstadoProtocolo,
    val rut: String,
    val fechaHora: Long,
    val motivo: String = ""
)

data class Protocolo(
    val numero: String,
    val visitaNumero: String?,
    val equipoId: String,
    val fechaHora: Long,
    val tecnicoRut: String,
    val temperatura: Double? = null,
    val humedad: Double? = null,
    val pruebas: List<Prueba> = emptyList(),
    val estado: EstadoProtocolo = EstadoProtocolo.BORRADOR,
    val observaciones: String = "",
    val mantenimiento: String = "",
    val motivoObservacion: String = "",
    val motivoAnulacion: String = "",
    val comentarioIngeniero: String = "",
    val reemplazaA: String? = null,
    val reemplazadoPor: String? = null,
    val historial: List<CambioEstado> = emptyList()
) {
    fun prueba(tipo: TipoPrueba): Prueba? = pruebas.firstOrNull { it.tipo == tipo }
}
