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
}
