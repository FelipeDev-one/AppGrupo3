package com.example.appgrupo3.model

import com.example.appgrupo3.utils.Tiempo
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object Reglas {
    const val DIAS_HABILES_FORMALIZACION = 2
    const val DIAS_AVISO_CALIBRACION = 30
    const val PENDIENTE = "Pendiente de formalización"

    fun puedeRegistrarDatos(u: Usuario): Boolean = u.perfil.esInterno && u.activo

    fun puedeCrearProtocolo(u: Usuario): Boolean = puedeRegistrarDatos(u)

    fun puedeCerrarVisita(u: Usuario): Boolean = u.perfil == Perfil.TECNICO || u.perfil == Perfil.INGENIERO

    fun esRevisor(u: Usuario): Boolean = u.perfil == Perfil.INGENIERO || u.perfil == Perfil.JEFE

    fun puedeGestionarInstrumentos(u: Usuario): Boolean = u.perfil == Perfil.JEFE

    fun puedeGestionarPersonal(u: Usuario): Boolean = u.perfil == Perfil.JEFE

    fun puedeEditar(p: Protocolo, u: Usuario): Boolean {
        if (p.estado != EstadoProtocolo.BORRADOR && p.estado != EstadoProtocolo.OBSERVADO) return false
        return when (u.perfil) {
            Perfil.TECNICO -> p.tecnicoRut == u.rut
            Perfil.INGENIERO, Perfil.JEFE -> true
            Perfil.CLIENTE -> false
        }
    }

    fun puedeDefinirResultadoManual(p: Protocolo, u: Usuario): Boolean =
        esRevisor(u) && p.estado != EstadoProtocolo.FORMALIZADO && p.estado != EstadoProtocolo.ANULADO

    fun puedeRegistrarEnTerreno(p: Protocolo, u: Usuario): Boolean =
        (u.perfil == Perfil.TECNICO || u.perfil == Perfil.INGENIERO) && puedeEditar(p, u)

    fun puedeFormalizar(p: Protocolo, u: Usuario): Boolean = esRevisor(u) && p.estado == EstadoProtocolo.REGISTRADO

    fun puedeObservar(p: Protocolo, u: Usuario): Boolean = puedeFormalizar(p, u)

    fun puedeAnular(p: Protocolo, u: Usuario): Boolean =
        esRevisor(u) && p.estado != EstadoProtocolo.FORMALIZADO && p.estado != EstadoProtocolo.ANULADO

    fun puedeReemplazar(p: Protocolo, u: Usuario): Boolean =
        esRevisor(u) && p.estado == EstadoProtocolo.FORMALIZADO && p.reemplazadoPor == null

    fun transicionPermitida(desde: EstadoProtocolo, hacia: EstadoProtocolo): Boolean = when (hacia) {
        EstadoProtocolo.REGISTRADO -> desde == EstadoProtocolo.BORRADOR || desde == EstadoProtocolo.OBSERVADO
        EstadoProtocolo.FORMALIZADO, EstadoProtocolo.OBSERVADO -> desde == EstadoProtocolo.REGISTRADO
        EstadoProtocolo.ANULADO -> desde != EstadoProtocolo.FORMALIZADO && desde != EstadoProtocolo.ANULADO
        EstadoProtocolo.BORRADOR -> false
    }

    fun puedeCambiar(p: Protocolo, hacia: EstadoProtocolo, u: Usuario): Boolean = when (hacia) {
        EstadoProtocolo.REGISTRADO -> puedeRegistrarEnTerreno(p, u)
        EstadoProtocolo.FORMALIZADO -> puedeFormalizar(p, u)
        EstadoProtocolo.OBSERVADO -> puedeObservar(p, u)
        EstadoProtocolo.ANULADO -> puedeAnular(p, u)
        EstadoProtocolo.BORRADOR -> false
    }

    fun requiereMotivo(hacia: EstadoProtocolo): Boolean =
        hacia == EstadoProtocolo.OBSERVADO || hacia == EstadoProtocolo.ANULADO

    fun faltantesRegistro(p: Protocolo, equipo: Equipo): List<String> {
        val lista = mutableListOf<String>()
        if (p.temperatura == null) lista += "Temperatura ambiente"
        if (p.humedad == null) lista += "Humedad relativa"
        val realizadasCompletas = p.pruebas.count { it.realizada && Calculos.estaCompleta(it, equipo) }
        if (realizadasCompletas == 0) lista += "Al menos una prueba completa"
        p.pruebas.filter { !Calculos.estaCompleta(it, equipo) }.forEach { lista += it.tipo.etiqueta }
        return lista
    }

    fun faltantesFormalizacion(p: Protocolo, equipo: Equipo, comparacion: List<ComparacionPrueba>): List<String> {
        val lista = mutableListOf<String>()
        p.pruebas.filter { it.realizada && Calculos.resultado(it, equipo) == null }
            .forEach { lista += "Resultado de ${it.tipo.etiqueta}" }
        if (comparacion.any { it.significativa } && p.comentarioIngeniero.isBlank()) {
            lista += "Comentario del ingeniero por variación significativa (RN-09)"
        }
        return lista
    }
}
