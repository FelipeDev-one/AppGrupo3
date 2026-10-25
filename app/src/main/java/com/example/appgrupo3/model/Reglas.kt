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
}
