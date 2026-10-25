package com.example.appgrupo3.model

import com.example.appgrupo3.utils.RutValidator
import com.example.appgrupo3.utils.Tiempo
import java.time.LocalDate

object Operaciones {

    private fun exigir(condicion: Boolean, mensaje: String) {
        if (!condicion) throw ReglaNegocioException(mensaje)
    }

    private fun protocoloOError(s: Sistema, numero: String): Protocolo =
        s.protocolo(numero) ?: throw ReglaNegocioException("El protocolo $numero no existe")

    private fun equipoOError(s: Sistema, id: String): Equipo =
        s.equipo(id) ?: throw ReglaNegocioException("El equipo no existe")

    private fun reemplazar(s: Sistema, protocolo: Protocolo): Sistema =
        s.copy(protocolos = s.protocolos.map { if (it.numero == protocolo.numero) protocolo else it })

    fun crearVisita(
        s: Sistema,
        clienteRut: String,
        instalacionId: String,
        fecha: LocalDate,
        tipo: TipoVisita,
        ingenieroRut: String,
        usuario: Usuario
    ): Pair<Sistema, Visita> {
        exigir(Reglas.puedeCrearProtocolo(usuario), "Tu perfil no puede crear visitas")
        val instalacion = s.instalacion(instalacionId)
        exigir(instalacion != null && instalacion.clienteRut == clienteRut, "La instalación no pertenece al cliente")
        exigir(s.usuario(ingenieroRut)?.perfil == Perfil.INGENIERO, "Toda visita necesita un ingeniero a cargo")
        val numero = Reglas.siguienteNumero("OT", s.visitas.map { it.numero }, fecha.year, s.dispositivo)
        val visita = Visita(numero, clienteRut, instalacionId, fecha.toString(), tipo, usuario.rut, ingenieroRut)
        return s.copy(visitas = s.visitas + visita) to visita
    }

    fun agregarProtocolo(s: Sistema, visitaNumero: String, equipoId: String, usuario: Usuario, ahora: Long): Pair<Sistema, Protocolo> {
        exigir(Reglas.puedeCrearProtocolo(usuario), "Tu perfil no puede crear protocolos")
        val visita = s.visita(visitaNumero) ?: throw ReglaNegocioException("La visita no existe")
        exigir(!visita.cerrada, "La visita ya está cerrada")
        val equipo = equipoOError(s, equipoId)
        exigir(equipo.instalacionId == visita.instalacionId, "El equipo no pertenece a la instalación de la visita")
        val repetido = s.protocolosDeVisita(visitaNumero).any {
            it.equipoId == equipoId && it.estado != EstadoProtocolo.ANULADO
        }
        exigir(!repetido, "El equipo ${equipo.tag} ya tiene un protocolo en esta visita")
        val fecha = LocalDate.parse(visita.fecha)
        val hora = Tiempo.aFecha(ahora).let { if (it == fecha) ahora else Tiempo.millisDe(fecha, 9, 0) }
        val numero = Reglas.siguienteNumero("PT", s.protocolos.map { it.numero }, fecha.year, s.dispositivo)
        val protocolo = Protocolo(
            numero = numero,
            visitaNumero = visitaNumero,
            equipoId = equipoId,
            fechaHora = hora,
            tecnicoRut = usuario.rut,
            pruebas = Catalogo.pruebasIniciales(equipo),
            historial = listOf(CambioEstado(null, EstadoProtocolo.BORRADOR, usuario.rut, ahora))
        )
        return s.copy(protocolos = s.protocolos + protocolo) to protocolo
    }
}
