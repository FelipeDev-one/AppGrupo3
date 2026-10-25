package com.example.appgrupo3.data

import com.example.appgrupo3.model.CambioEstado
import com.example.appgrupo3.model.Cliente
import com.example.appgrupo3.model.Equipo
import com.example.appgrupo3.model.EstadoProtocolo
import com.example.appgrupo3.model.Instalacion
import com.example.appgrupo3.model.Instrumento
import com.example.appgrupo3.model.Perfil
import com.example.appgrupo3.model.Protocolo
import com.example.appgrupo3.model.Prueba
import com.example.appgrupo3.model.Recepcion
import com.example.appgrupo3.model.Resultado
import com.example.appgrupo3.model.Sistema
import com.example.appgrupo3.model.TipoEquipo
import com.example.appgrupo3.model.TipoInstrumento
import com.example.appgrupo3.model.TipoPrueba
import com.example.appgrupo3.model.TipoVisita
import com.example.appgrupo3.model.UnidadAislacion
import com.example.appgrupo3.model.Usuario
import com.example.appgrupo3.model.Visita
import com.example.appgrupo3.utils.Tiempo
import kotlin.random.Random

object DatosEjemplo {
    private const val ENVASADORA = "76.482.915-8"
    private const val SANITARIA = "96.731.540-0"
    private const val MINERA = "77.105.362-9"
    private const val CLINICA = "76.903.218-5"
    private const val CEMENTOS = "99.524.170-6"
    private const val DATACENTER = "78.214.635-1"

    private const val GODOY = "17.284.519-3"
    private const val TAPIA = "19.412.087-7"
    private const val RONDON = "26.104.583-4"
    private const val VALENZUELA = "20.137.648-3"
    private const val CONTRERAS = "15.906.334-8"
    private const val MORALES = "16.553.270-8"
    private const val ESPINOZA = "13.871.905-7"

    private const val MEGGER_5KV = "MIT-0045821"
    private const val TTR300 = "TTR-30044"
    private const val DLRO = "DLR-10882"
    private const val CMC = "CMC-HB411"

    fun codigoDispositivo(): String {
        val letras = "ABCDEFGHJKLMNPQRSTUVWXYZ"
        return "${letras[Random.nextInt(letras.length)]}${Random.nextInt(10)}"
    }

    fun crear(dispositivo: String = codigoDispositivo()): Sistema = Sistema(
        clientes = clientes,
        instalaciones = instalaciones,
        equipos = equipos,
        usuarios = usuarios,
        instrumentos = instrumentos,
        visitas = visitas,
        protocolos = protocolos,
        dispositivo = dispositivo
    )

    private val clientes = listOf(
        Cliente(ENVASADORA, "Envasadora del Pacífico S.A.", "Marcela Fuentes Rojas", "mfuentes@envpacifico.ejemplo"),
        Cliente(SANITARIA, "Sanitaria Valle Central S.A.", "Jorge Arancibia Leiva", "jarancibia@svcentral.ejemplo"),
        Cliente(MINERA, "Minera Cerro Alto SpA", "Patricio Navarro Soto", "pnavarro@cerroalto.ejemplo"),
        Cliente(CLINICA, "Clínica Los Aromos Ltda.", "Carolina Méndez Pizarro", "cmendez@losaromos.ejemplo"),
        Cliente(CEMENTOS, "Cementos Lomas Blancas S.A.", "Luis Bravo Castillo", "lbravo@lomasblancas.ejemplo"),
        Cliente(DATACENTER, "Data Center Andes Norte SpA", "Felipe Órdenes Vera", "fordenes@andesnorte.ejemplo")
    )

    private val instalaciones = listOf(
        Instalacion("INS-01", ENVASADORA, "Planta Lampa", "Lampa"),
        Instalacion("INS-02", SANITARIA, "Planta Elevadora Sur", "San Bernardo", sinSenal = true),
        Instalacion("INS-03", MINERA, "Faena Til Til", "Til Til", sinSenal = true),
        Instalacion("INS-04", CLINICA, "Edificio Clínico", "La Florida"),
        Instalacion("INS-05", CEMENTOS, "Planta Molienda", "Puente Alto"),
        Instalacion("INS-06", DATACENTER, "Site Huechuraba", "Huechuraba")
    )

    private val equipos = listOf(
        Equipo("EQ-01", "INS-01", "TR-01", TipoEquipo.TRANSFORMADOR, "Rhona", "RH-18733", 2012, "1.500 kVA, 12 / 0,4 kV"),
        Equipo("EQ-02", "INS-01", "TR-02", TipoEquipo.TRANSFORMADOR, "Rhona", "RH-21904", 2017, "1.000 kVA, 12 / 0,4 kV"),
        Equipo("EQ-03", "INS-02", "TR-A", TipoEquipo.TRANSFORMADOR, "Tusan", "TS-5521", 2009, "2.500 kVA, 23 / 0,4 kV"),
        Equipo("EQ-04", "INS-04", "CEL-MT-03", TipoEquipo.CELDA_MT, "Ormazabal", "OR-77812", 2018, "12 kV, 630 A", contactoMaximo = 60.0),
        Equipo("EQ-05", "INS-03", "INT-52-1", TipoEquipo.INTERRUPTOR, "Schneider", "SE-449021", 2015, "23 kV, 1.250 A", contactoMaximo = 45.0),
        Equipo("EQ-06", "INS-05", "REL-50/51-A", TipoEquipo.RELE, "SEL", "SEL-751-30219", 2020, "Sobrecorriente", ajusteRele = 5.0),
        Equipo("EQ-07", "INS-06", "CAB-MT-02", TipoEquipo.CABLE, "Nexans", "NX-2201-B", 2019, "15 kV, 3×240 mm², 180 m"),
        Equipo("EQ-08", "INS-03", "TR-SS-01", TipoEquipo.TRANSFORMADOR, "ABB", "ABB-1180442", 2011, "5.000 kVA, 23 / 6,6 kV")
    )

    private val usuarios = listOf(
        Usuario(GODOY, "Matías Godoy Herrera", Perfil.TECNICO),
        Usuario(TAPIA, "Javiera Tapia Muñoz", Perfil.TECNICO),
        Usuario(RONDON, "Carlos Rondón Pérez", Perfil.TECNICO),
        Usuario(VALENZUELA, "Diego Valenzuela Ríos", Perfil.TECNICO),
        Usuario(CONTRERAS, "Andrea Contreras Silva", Perfil.INGENIERO),
        Usuario(MORALES, "Sebastián Morales Gutiérrez", Perfil.INGENIERO),
        Usuario(ESPINOZA, "Ricardo Espinoza Lagos", Perfil.JEFE),
        Usuario("14.226.791-8", "Marcela Fuentes Rojas", Perfil.CLIENTE, "Jefa de Mantenimiento", ENVASADORA),
        Usuario("18.390.452-3", "Tomás Leiva Araya", Perfil.CLIENTE, "Supervisor Eléctrico", CLINICA),
        Usuario("12.645.308-6", "Patricio Navarro Soto", Perfil.CLIENTE, "Jefe de Mantenimiento Eléctrico", MINERA)
    )

    private val instrumentos = listOf(
        Instrumento(MEGGER_5KV, TipoInstrumento.MEGOHMETRO, "Megóhmetro 5 kV", "Megger MIT525", "2026-03-15", "2027-03-15"),
        Instrumento("MIT-0071233", TipoInstrumento.MEGOHMETRO, "Megóhmetro 10 kV", "Megger MIT1025", "2025-10-10", "2026-10-10"),
        Instrumento(TTR300, TipoInstrumento.TTR, "Medidor de relación (TTR)", "Megger TTR300", "2026-06-02", "2027-06-02"),
        Instrumento(DLRO, TipoInstrumento.MICROOHMIMETRO, "Micro-ohmímetro", "Megger DLRO10X", "2025-08-20", "2026-08-20"),
        Instrumento(CMC, TipoInstrumento.INYECCION, "Equipo de inyección secundaria", "Omicron CMC 356", "2026-01-05", "2027-01-05")
    )

    private val visitas = listOf(
        Visita(
            "OT-2026-1187", ENVASADORA, "INS-01", "2026-09-26", TipoVisita.PRUEBAS_Y_MANTENIMIENTO, GODOY, CONTRERAS,
            Recepcion("Marcela Fuentes Rojas", "14.226.791-8", "Jefa de Mantenimiento", Tiempo.millis(2026, 9, 26, 13, 42))
        ),
        Visita(
            "OT-2026-1154", CLINICA, "INS-04", "2026-09-20", TipoVisita.SOLO_PRUEBAS, RONDON, MORALES,
            Recepcion("Tomás Leiva Araya", "18.390.452-3", "Supervisor Eléctrico", Tiempo.millis(2026, 9, 20, 9, 55))
        )
    )

    private fun aislacionTrafo(atBt: Pair<Double, Double>, atT: Pair<Double, Double>, btT: Pair<Double, Double>) = Prueba(
        tipo = TipoPrueba.AISLACION,
        instrumentoSerie = MEGGER_5KV,
        tensionKv = 5.0,
        unidad = UnidadAislacion.MEGA,
        valores = mapOf(
            "AT-BT_1" to atBt.first, "AT-BT_10" to atBt.second,
            "AT-T_1" to atT.first, "AT-T_10" to atT.second,
            "BT-T_1" to btT.first, "BT-T_10" to btT.second
        )
    )

    private fun ttr(h1: Double, h2: Double, h3: Double, tap: Double = 3.0) = Prueba(
        tipo = TipoPrueba.TTR,
        instrumentoSerie = TTR300,
        valores = mapOf("teorica" to 30.0, "H1-X1" to h1, "H2-X2" to h2, "H3-X3" to h3, "tap" to tap)
    )

    private fun devanados(at: List<Double>, bt: List<Double>, resultado: Resultado?) = Prueba(
        tipo = TipoPrueba.DEVANADOS,
        instrumentoSerie = DLRO,
        valores = mapOf(
            "AT_R" to at[0], "AT_S" to at[1], "AT_T" to at[2],
            "BT_R" to bt[0], "BT_S" to bt[1], "BT_T" to bt[2]
        ),
        resultadoManual = resultado
    )

    private fun historial(vararg cambios: Pair<EstadoProtocolo, Long>, rut: String, motivo: String = ""): List<CambioEstado> {
        var anterior: EstadoProtocolo? = null
        return cambios.map { (estado, fecha) ->
            val cambio = CambioEstado(anterior, estado, rut, fecha, if (estado == EstadoProtocolo.OBSERVADO) motivo else "")
            anterior = estado
            cambio
        }
    }

    private val protocolos: List<Protocolo> = run {
        val f2024 = Tiempo.millis(2024, 9, 14, 10, 0)
        val f2025 = Tiempo.millis(2025, 9, 13, 10, 30)
        val f0401 = Tiempo.millis(2026, 9, 26, 9, 40)
        val f0402 = Tiempo.millis(2026, 9, 26, 11, 15)
        val f0389 = Tiempo.millis(2026, 9, 20, 7, 30)
        val f0377 = Tiempo.millis(2026, 9, 15, 10, 0)
        val f0360 = Tiempo.millis(2026, 9, 8, 9, 0)
        val dia = 86_400_000L
        listOf(
            Protocolo(
                numero = "PT-2024-0312", visitaNumero = null, equipoId = "EQ-01", fechaHora = f2024, tecnicoRut = GODOY,
                temperatura = 18.0, humedad = 55.0,
                pruebas = listOf(
                    aislacionTrafo(11000.0 to 26400.0, 12500.0 to 31250.0, 5200.0 to 12480.0),
                    ttr(30.02, 30.03, 30.01),
                    devanados(listOf(1838.0, 1845.0, 1836.0), listOf(0.410, 0.407, 0.412), Resultado.APROBADO)
                ),
                estado = EstadoProtocolo.FORMALIZADO,
                historial = historial(
                    EstadoProtocolo.BORRADOR to f2024, EstadoProtocolo.REGISTRADO to f2024 + 3_600_000,
                    EstadoProtocolo.FORMALIZADO to f2024 + dia * 2, rut = CONTRERAS
                )
            ),
            Protocolo(
                numero = "PT-2025-0288", visitaNumero = null, equipoId = "EQ-01", fechaHora = f2025, tecnicoRut = TAPIA,
                temperatura = 21.0, humedad = 48.0,
                pruebas = listOf(
                    aislacionTrafo(9500.0 to 21850.0, 9800.0 to 21560.0, 4300.0 to 9890.0),
                    ttr(30.03, 30.04, 30.02),
                    devanados(listOf(1840.0, 1848.0, 1838.0), listOf(0.411, 0.408, 0.414), Resultado.APROBADO)
                ),
                estado = EstadoProtocolo.FORMALIZADO,
                historial = historial(
                    EstadoProtocolo.BORRADOR to f2025, EstadoProtocolo.REGISTRADO to f2025 + 3_600_000,
                    EstadoProtocolo.FORMALIZADO to f2025 + dia, rut = CONTRERAS
                )
            ),
            Protocolo(
                numero = "PT-2026-0401", visitaNumero = "OT-2026-1187", equipoId = "EQ-01", fechaHora = f0401, tecnicoRut = GODOY,
                temperatura = 19.0, humedad = 60.0,
                pruebas = listOf(
                    aislacionTrafo(5800.0 to 11200.0, 6100.0 to 10980.0, 3200.0 to 6900.0).copy(unidad = UnidadAislacion.GIGA),
                    ttr(30.04, 30.06, 29.98),
                    devanados(listOf(1842.0, 1850.0, 1839.0), listOf(0.412, 0.409, 0.415), null)
                ),
                estado = EstadoProtocolo.REGISTRADO,
                observaciones = "IP bajo en AT-T respecto del año anterior. Leve suciedad en aisladores BT.",
                mantenimiento = "Limpieza de aisladores BT. No se repitió la medición por término de la ventana de corte.",
                historial = historial(EstadoProtocolo.BORRADOR to f0401, EstadoProtocolo.REGISTRADO to f0401 + 3 * 3_600_000, rut = GODOY)
            ),
            Protocolo(
                numero = "PT-2026-0402", visitaNumero = "OT-2026-1187", equipoId = "EQ-02", fechaHora = f0402, tecnicoRut = GODOY,
                temperatura = 19.0, humedad = 60.0,
                pruebas = listOf(
                    aislacionTrafo(8200.0 to 19680.0, 8900.0 to 21360.0, 4100.0 to 9430.0),
                    ttr(30.02, 30.01, 30.03),
                    devanados(listOf(2010.0, 2016.0, 2008.0), listOf(0.520, 0.518, 0.522), null)
                ),
                estado = EstadoProtocolo.REGISTRADO,
                mantenimiento = "Reapriete de conexiones en bornes BT.",
                historial = historial(EstadoProtocolo.BORRADOR to f0402, EstadoProtocolo.REGISTRADO to f0402 + 2 * 3_600_000, rut = GODOY)
            ),
            Protocolo(
                numero = "PT-2026-0389", visitaNumero = "OT-2026-1154", equipoId = "EQ-04", fechaHora = f0389, tecnicoRut = RONDON,
                temperatura = 22.0, humedad = 45.0,
                pruebas = listOf(
                    Prueba(TipoPrueba.AISLACION, instrumentoSerie = MEGGER_5KV, tensionKv = 5.0, valores = mapOf("L_1" to 25000.0)),
                    Prueba(TipoPrueba.CONTACTOS, instrumentoSerie = null, valores = mapOf("P1" to 58.0, "P2" to 66.0, "P3" to 61.0))
                ),
                estado = EstadoProtocolo.OBSERVADO,
                motivoObservacion = "Falta el número de serie del instrumento en la prueba de contactos",
                historial = historial(
                    EstadoProtocolo.BORRADOR to f0389, EstadoProtocolo.REGISTRADO to f0389 + 2 * 3_600_000,
                    EstadoProtocolo.OBSERVADO to f0389 + dia, rut = MORALES,
                    motivo = "Falta el número de serie del instrumento en la prueba de contactos"
                )
            ),
            Protocolo(
                numero = "PT-2026-0377", visitaNumero = null, equipoId = "EQ-06", fechaHora = f0377, tecnicoRut = VALENZUELA,
                temperatura = 24.0, humedad = 35.0,
                pruebas = listOf(
                    Prueba(
                        TipoPrueba.OPERACION_RELE, instrumentoSerie = CMC,
                        valores = mapOf("ajuste" to 5.0, "corriente" to 5.02, "t_esperado" to 300.0, "t_medido" to 309.0)
                    )
                ),
                estado = EstadoProtocolo.FORMALIZADO,
                historial = historial(
                    EstadoProtocolo.BORRADOR to f0377, EstadoProtocolo.REGISTRADO to f0377 + 3_600_000,
                    EstadoProtocolo.FORMALIZADO to f0377 + dia, rut = CONTRERAS
                )
            ),
            Protocolo(
                numero = "PT-2026-0360", visitaNumero = null, equipoId = "EQ-05", fechaHora = f0360, tecnicoRut = TAPIA,
                temperatura = 27.0, humedad = 20.0,
                pruebas = listOf(
                    Prueba(TipoPrueba.AISLACION, instrumentoSerie = MEGGER_5KV, tensionKv = 5.0, valores = mapOf("L_1" to 18000.0)),
                    Prueba(TipoPrueba.CONTACTOS, instrumentoSerie = DLRO, valores = mapOf("P1" to 44.0, "P2" to 58.0, "P3" to 47.0))
                ),
                estado = EstadoProtocolo.FORMALIZADO,
                comentarioIngeniero = "Polo 2 sobre 120 % del máximo de placa. Se recomienda revisión de contactos.",
                historial = historial(
                    EstadoProtocolo.BORRADOR to f0360, EstadoProtocolo.REGISTRADO to f0360 + 3_600_000,
                    EstadoProtocolo.FORMALIZADO to f0360 + dia, rut = MORALES
                )
            )
        )
    }
}
