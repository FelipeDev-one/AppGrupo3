package com.example.appgrupo3

import com.example.appgrupo3.data.DatosEjemplo
import com.example.appgrupo3.model.Calculos
import com.example.appgrupo3.model.EstadoCalibracion
import com.example.appgrupo3.model.EstadoProtocolo
import com.example.appgrupo3.model.Operaciones
import com.example.appgrupo3.model.Prueba
import com.example.appgrupo3.model.Recepcion
import com.example.appgrupo3.model.ReglaNegocioException
import com.example.appgrupo3.model.Reglas
import com.example.appgrupo3.model.Resultado
import com.example.appgrupo3.model.TagDuplicadoException
import com.example.appgrupo3.model.TipoPrueba
import com.example.appgrupo3.model.TipoVisita
import com.example.appgrupo3.utils.RutValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReglasNegocioTest {
    private val base = DatosEjemplo.crear("T1")
    private val tecnico = base.usuario("17.284.519-3")!!
    private val otroTecnico = base.usuario("19.412.087-7")!!
    private val ingeniero = base.usuario("15.906.334-8")!!
    private val jefe = base.usuario("13.871.905-7")!!
    private val cliente = base.usuario("14.226.791-8")!!
    private val ahora = System.currentTimeMillis()

    @Test
    fun ruts_del_anexo_tienen_digito_valido() {
        base.usuarios.forEach { assertTrue(it.rut, RutValidator.esValido(it.rut)) }
        base.clientes.forEach { assertTrue(it.rut, RutValidator.esValido(it.rut)) }
        assertFalse(RutValidator.esValido("17.284.519-4"))
    }

    @Test
    fun indice_de_polarizacion_clasifica_segun_rn06() {
        assertEquals(Resultado.APROBADO, Calculos.clasificarIp(Calculos.indicePolarizacion(12500.0, 31250.0)))
        assertEquals(Resultado.OBSERVADO, Calculos.clasificarIp(Calculos.indicePolarizacion(6100.0, 10980.0)))
        assertEquals(Resultado.RECHAZADO, Calculos.clasificarIp(0.9))
    }

    @Test
    fun lectura_bajo_100_megaohm_rechaza_la_aislacion() {
        val equipo = base.equipo("EQ-07")!!
        val prueba = Prueba(TipoPrueba.AISLACION, valores = mapOf("R_1" to 90.0, "R_10" to 300.0, "S_1" to 500.0, "S_10" to 1500.0, "T_1" to 500.0, "T_10" to 1500.0))
        assertEquals(Resultado.RECHAZADO, Calculos.resultado(prueba, equipo))
    }

    @Test
    fun error_ttr_contactos_y_rele() {
        assertEquals(Resultado.APROBADO, Calculos.clasificarErrorTtr(Calculos.errorTtr(30.0, 30.06)))
        assertEquals(Resultado.OBSERVADO, Calculos.clasificarErrorTtr(Calculos.errorTtr(30.0, 30.27)))
        assertEquals(Resultado.RECHAZADO, Calculos.clasificarErrorTtr(Calculos.errorTtr(30.0, 30.4)))
        assertEquals(Resultado.OBSERVADO, Calculos.clasificarContactos(Calculos.porcentajeContactos(66.0, 60.0)))
        assertEquals(Resultado.RECHAZADO, Calculos.clasificarContactos(Calculos.porcentajeContactos(58.0, 45.0)))
        assertEquals(Resultado.APROBADO, Calculos.clasificarRele(Calculos.diferenciaRele(300.0, 309.0)))
        assertEquals(Resultado.OBSERVADO, Calculos.clasificarRele(Calculos.diferenciaRele(300.0, 325.0)))
    }

    @Test
    fun rele_que_no_opera_queda_rechazado() {
        val equipo = base.equipo("EQ-06")!!
        val prueba = Prueba(TipoPrueba.OPERACION_RELE, noOpera = true, valores = mapOf("ajuste" to 5.0, "t_esperado" to 300.0))
        assertEquals(Resultado.RECHAZADO, Calculos.resultado(prueba, equipo))
    }

    @Test
    fun resultado_global_es_el_peor_y_omite_no_realizadas() {
        val p = base.protocolo("PT-2026-0401")!!
        val equipo = base.equipo(p.equipoId)!!
        assertEquals(Resultado.OBSERVADO, Calculos.global(p, equipo))
        val conNoRealizada = p.copy(pruebas = p.pruebas.map {
            if (it.tipo == TipoPrueba.AISLACION) it.copy(realizada = false, motivoNoRealizada = "Término de ventana de corte") else it
        })
        assertEquals(Resultado.APROBADO, Calculos.global(conNoRealizada, equipo))
    }

    @Test
    fun variacion_mayor_a_25_por_ciento_es_significativa() {
        val p = base.protocolo("PT-2026-0401")!!
        val equipo = base.equipo(p.equipoId)!!
        val anterior = base.protocoloAnterior(p)
        assertEquals("PT-2025-0288", anterior?.numero)
        val aislacion = Calculos.comparar(p, anterior, equipo).first { it.tipo == TipoPrueba.AISLACION }
        val atT = aislacion.variaciones.first { it.campo.clave == "AT-T_1" }
        assertEquals(-37.8, atT.porcentaje, 0.1)
        assertTrue(aislacion.significativa)

        val p2025 = base.protocolo("PT-2025-0288")!!
        val previa = Calculos.comparar(p2025, base.protocoloAnterior(p2025), equipo).first { it.tipo == TipoPrueba.AISLACION }
        assertEquals(-21.6, previa.variaciones.first { it.campo.clave == "AT-T_1" }.porcentaje, 0.1)
        assertFalse(previa.significativa)
    }

    @Test
    fun formalizar_exige_comentario_por_variacion_y_resultado_manual() {
        var s = base
        val numero = "PT-2026-0401"
        val error = runCatching { Operaciones.cambiarEstado(s, numero, EstadoProtocolo.FORMALIZADO, "", ingeniero, ahora) }.exceptionOrNull()
        assertTrue(error is ReglaNegocioException)
        s = Operaciones.definirResultadoManual(s, numero, TipoPrueba.DEVANADOS, Resultado.APROBADO, ingeniero)
        s = Operaciones.guardarComentarioIngeniero(s, numero, "Baja de aislación AT-T, se recomienda adelantar la próxima prueba", ingeniero)
        s = Operaciones.cambiarEstado(s, numero, EstadoProtocolo.FORMALIZADO, "", ingeniero, ahora)
        assertEquals(EstadoProtocolo.FORMALIZADO, s.protocolo(numero)!!.estado)
    }

    @Test
    fun tecnico_no_formaliza_y_formalizado_no_se_anula() {
        val numero = "PT-2026-0402"
        assertTrue(runCatching { Operaciones.cambiarEstado(base, numero, EstadoProtocolo.FORMALIZADO, "", tecnico, ahora) }.isFailure)
        assertTrue(runCatching { Operaciones.cambiarEstado(base, "PT-2026-0377", EstadoProtocolo.ANULADO, "Error de digitación", jefe, ahora) }.isFailure)
    }

    @Test
    fun observar_y_anular_exigen_motivo() {
        assertTrue(runCatching { Operaciones.cambiarEstado(base, "PT-2026-0402", EstadoProtocolo.OBSERVADO, " ", ingeniero, ahora) }.isFailure)
        val s = Operaciones.cambiarEstado(base, "PT-2026-0402", EstadoProtocolo.OBSERVADO, "Revisar tap", ingeniero, ahora)
        assertEquals("Revisar tap", s.protocolo("PT-2026-0402")!!.motivoObservacion)
    }

    @Test
    fun tecnico_solo_edita_sus_protocolos_en_borrador_u_observado() {
        val observado = base.protocolo("PT-2026-0389")!!
        assertFalse(Reglas.puedeEditar(observado, tecnico))
        assertTrue(Reglas.puedeEditar(observado, base.usuario("26.104.583-4")!!))
        assertFalse(Reglas.puedeEditar(base.protocolo("PT-2026-0402")!!, tecnico))
        assertFalse(Reglas.puedeEditar(observado, cliente))
    }

    @Test
    fun flujo_completo_de_visita_con_numeros_unicos() {
        val hoy = LocalDate.now()
        var (s, visita) = Operaciones.crearVisita(base, "77.105.362-9", "INS-03", hoy, TipoVisita.SOLO_PRUEBAS, "16.553.270-8", otroTecnico)
        val (s2, protocolo) = Operaciones.agregarProtocolo(s, visita.numero, "EQ-05", otroTecnico, ahora)
        s = s2
        assertTrue(protocolo.numero.endsWith("-T1"))
        assertTrue(runCatching { Operaciones.agregarProtocolo(s, visita.numero, "EQ-05", otroTecnico, ahora) }.isFailure)
        val (s3, otro) = Operaciones.agregarProtocolo(s, visita.numero, "EQ-08", otroTecnico, ahora)
        s = s3
        assertNotEquals(protocolo.numero, otro.numero)

        val recepcion = Recepcion("Patricio Navarro Soto", "12.645.308-6", "Jefe de Mantenimiento Eléctrico", ahora)
        assertTrue(runCatching { Operaciones.cerrarVisita(s, visita.numero, recepcion, otroTecnico) }.isFailure)

        s = Operaciones.cambiarEstado(s, otro.numero, EstadoProtocolo.ANULADO, "Equipo fuera de servicio", ingeniero, ahora)
        s = Operaciones.guardarDatosGenerales(s, protocolo.numero, 25.0, 30.0, "", "", otroTecnico)
        s = Operaciones.guardarPrueba(
            s, protocolo.numero,
            Prueba(TipoPrueba.AISLACION, instrumentoSerie = "MIT-0045821", tensionKv = 5.0, valores = mapOf("L_1" to 20000.0)),
            otroTecnico
        )
        assertTrue(runCatching { Operaciones.cambiarEstado(s, protocolo.numero, EstadoProtocolo.REGISTRADO, "", otroTecnico, ahora) }.isFailure)
        s = Operaciones.guardarPrueba(s, protocolo.numero, Prueba(TipoPrueba.CONTACTOS, realizada = false, motivoNoRealizada = "Cliente no autorizó desconexión"), otroTecnico)
        s = Operaciones.cambiarEstado(s, protocolo.numero, EstadoProtocolo.REGISTRADO, "", otroTecnico, ahora)
        s = Operaciones.cerrarVisita(s, visita.numero, recepcion, otroTecnico)
        assertTrue(s.visita(visita.numero)!!.cerrada)
    }

    @Test
    fun instrumento_vencido_no_se_puede_usar() {
        val micro = base.instrumento("DLR-10882")!!
        assertEquals(EstadoCalibracion.VENCIDO, Reglas.estadoCalibracion(micro, LocalDate.of(2026, 10, 24)))
        val (s, visita) = Operaciones.crearVisita(base, "76.903.218-5", "INS-04", LocalDate.now(), TipoVisita.SOLO_PRUEBAS, "16.553.270-8", tecnico)
        val (s2, p) = Operaciones.agregarProtocolo(s, visita.numero, "EQ-04", tecnico, ahora)
        val prueba = Prueba(TipoPrueba.CONTACTOS, instrumentoSerie = "DLR-10882", valores = mapOf("P1" to 50.0, "P2" to 51.0, "P3" to 52.0))
        assertTrue(runCatching { Operaciones.guardarPrueba(s2, p.numero, prueba, tecnico) }.isFailure)
        assertTrue(runCatching { Operaciones.guardarInstrumento(s2, micro.copy(vencimiento = "2027-08-20"), tecnico, false) }.isFailure)
        val actualizado = Operaciones.guardarInstrumento(s2, micro.copy(calibracion = "2026-10-20", vencimiento = "2027-10-20"), jefe, false)
        Operaciones.guardarPrueba(actualizado, p.numero, prueba, tecnico)
    }

    @Test
    fun tag_unico_por_instalacion() {
        val existente = base.equipo("EQ-01")!!
        val duplicado = existente.copy(id = "EQ-99", serie = "OTRA")
        val error = runCatching { Operaciones.guardarEquipo(base, duplicado, tecnico) }.exceptionOrNull()
        assertTrue(error is TagDuplicadoException)
        Operaciones.guardarEquipo(base, duplicado.copy(instalacionId = "INS-02"), tecnico)
    }

    @Test
    fun atrasado_despues_de_dos_dias_habiles() {
        val p = base.protocolo("PT-2026-0402")!!
        assertFalse(Reglas.estaAtrasado(p, LocalDate.of(2026, 9, 29)))
        assertTrue(Reglas.estaAtrasado(p, LocalDate.of(2026, 9, 30)))
        assertFalse(Reglas.estaAtrasado(base.protocolo("PT-2026-0377")!!, LocalDate.of(2026, 10, 24)))
    }

    @Test
    fun cliente_ve_pendiente_de_formalizacion() {
        assertEquals(Reglas.PENDIENTE, Reglas.estadoParaCliente(base.protocolo("PT-2026-0401")!!))
        assertEquals(Reglas.PENDIENTE, Reglas.estadoParaCliente(base.protocolo("PT-2026-0389")!!))
        assertFalse(Reglas.visibleParaCliente(base.protocolo("PT-2026-0401")!!.copy(estado = EstadoProtocolo.BORRADOR)))
    }

    @Test
    fun reemplazo_marca_al_original() {
        val (s, nuevo) = Operaciones.emitirReemplazo(base, "PT-2026-0377", ingeniero, ahora)
        assertEquals(nuevo.numero, s.protocolo("PT-2026-0377")!!.reemplazadoPor)
        assertEquals("PT-2026-0377", nuevo.reemplazaA)
        assertEquals(EstadoProtocolo.BORRADOR, nuevo.estado)
        assertNull(nuevo.reemplazadoPor)
    }
}
