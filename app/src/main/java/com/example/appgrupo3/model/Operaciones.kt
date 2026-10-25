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

    fun guardarDatosGenerales(
        s: Sistema,
        numero: String,
        temperatura: Double?,
        humedad: Double?,
        observaciones: String,
        mantenimiento: String,
        usuario: Usuario
    ): Sistema {
        val p = protocoloOError(s, numero)
        exigir(Reglas.puedeEditar(p, usuario), "No puedes modificar este protocolo (RN-17)")
        if (temperatura != null) exigir(temperatura in -20.0..60.0, "La temperatura debe estar entre -20 y 60 °C")
        if (humedad != null) exigir(humedad in 0.0..100.0, "La humedad debe estar entre 0 y 100 %")
        return reemplazar(
            s,
            p.copy(temperatura = temperatura, humedad = humedad, observaciones = observaciones.trim(), mantenimiento = mantenimiento.trim())
        )
    }

    fun guardarPrueba(s: Sistema, numero: String, prueba: Prueba, usuario: Usuario): Sistema {
        val p = protocoloOError(s, numero)
        exigir(Reglas.puedeEditar(p, usuario), "No puedes modificar este protocolo (RN-17)")
        if (prueba.realizada) {
            val serie = prueba.instrumentoSerie
            if (serie != null) {
                val instrumento = s.instrumento(serie) ?: throw ReglaNegocioException("El instrumento no existe")
                val fecha = Tiempo.aFecha(p.fechaHora)
                exigir(prueba.tipo in instrumento.tipo.pruebas, "${instrumento.nombre} no sirve para ${prueba.tipo.etiqueta}")
                exigir(
                    Reglas.estadoCalibracion(instrumento, fecha) != EstadoCalibracion.VENCIDO,
                    "La calibración de ${instrumento.serie} venció el ${Tiempo.fechaLegible(instrumento.vencimiento)} (RN-11)"
                )
            }
        } else {
            exigir(prueba.motivoNoRealizada.isNotBlank(), "Indica por qué no se realizó la prueba")
        }
        val manual = if (Reglas.esRevisor(usuario)) prueba.resultadoManual else p.prueba(prueba.tipo)?.resultadoManual
        val final = prueba.copy(resultadoManual = manual)
        return reemplazar(s, p.copy(pruebas = p.pruebas.map { if (it.tipo == prueba.tipo) final else it }))
    }

    fun definirResultadoManual(s: Sistema, numero: String, tipo: TipoPrueba, resultado: Resultado, usuario: Usuario): Sistema {
        val p = protocoloOError(s, numero)
        exigir(Reglas.puedeDefinirResultadoManual(p, usuario), "Solo el ingeniero o jefe técnico define este resultado (RN-08c)")
        return reemplazar(s, p.copy(pruebas = p.pruebas.map { if (it.tipo == tipo) it.copy(resultadoManual = resultado) else it }))
    }

    fun guardarComentarioIngeniero(s: Sistema, numero: String, comentario: String, usuario: Usuario): Sistema {
        val p = protocoloOError(s, numero)
        exigir(Reglas.puedeDefinirResultadoManual(p, usuario), "Solo el ingeniero o jefe técnico puede comentar")
        return reemplazar(s, p.copy(comentarioIngeniero = comentario.trim()))
    }
}
