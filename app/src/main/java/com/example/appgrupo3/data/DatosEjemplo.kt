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
}
