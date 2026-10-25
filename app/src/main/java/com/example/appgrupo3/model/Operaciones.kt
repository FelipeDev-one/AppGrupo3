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

    fun cambiarEstado(
        s: Sistema,
        numero: String,
        hacia: EstadoProtocolo,
        motivo: String,
        usuario: Usuario,
        ahora: Long
    ): Sistema {
        val p = protocoloOError(s, numero)
        val equipo = equipoOError(s, p.equipoId)
        exigir(Reglas.transicionPermitida(p.estado, hacia), "No se puede pasar de ${p.estado.etiqueta} a ${hacia.etiqueta} (RN-04)")
        exigir(Reglas.puedeCambiar(p, hacia, usuario), "Tu perfil no puede dejar el protocolo en ${hacia.etiqueta}")
        if (Reglas.requiereMotivo(hacia)) exigir(motivo.isNotBlank(), "El motivo es obligatorio")
        when (hacia) {
            EstadoProtocolo.REGISTRADO -> {
                val faltan = Reglas.faltantesRegistro(p, equipo)
                exigir(faltan.isEmpty(), "Falta completar: " + faltan.joinToString(", "))
            }
            EstadoProtocolo.FORMALIZADO -> {
                val comparacion = Calculos.comparar(p, s.protocoloAnterior(p), equipo)
                val faltan = Reglas.faltantesFormalizacion(p, equipo, comparacion)
                exigir(faltan.isEmpty(), "Falta completar: " + faltan.joinToString(", "))
            }
            else -> Unit
        }
        val cambio = CambioEstado(p.estado, hacia, usuario.rut, ahora, motivo.trim())
        val nuevo = p.copy(
            estado = hacia,
            motivoObservacion = if (hacia == EstadoProtocolo.OBSERVADO) motivo.trim() else p.motivoObservacion,
            motivoAnulacion = if (hacia == EstadoProtocolo.ANULADO) motivo.trim() else p.motivoAnulacion,
            historial = p.historial + cambio
        )
        return reemplazar(s, nuevo)
    }

    fun emitirReemplazo(s: Sistema, numero: String, usuario: Usuario, ahora: Long): Pair<Sistema, Protocolo> {
        val original = protocoloOError(s, numero)
        exigir(Reglas.puedeReemplazar(original, usuario), "Solo se reemplaza un protocolo formalizado, por ingeniero o jefe técnico")
        val anio = Tiempo.aFecha(ahora).year
        val nuevoNumero = Reglas.siguienteNumero("PT", s.protocolos.map { it.numero }, anio, s.dispositivo)
        val nuevo = original.copy(
            numero = nuevoNumero,
            estado = EstadoProtocolo.BORRADOR,
            tecnicoRut = usuario.rut,
            motivoObservacion = "",
            motivoAnulacion = "",
            reemplazaA = original.numero,
            reemplazadoPor = null,
            historial = listOf(CambioEstado(null, EstadoProtocolo.BORRADOR, usuario.rut, ahora, "Reemplaza a ${original.numero}"))
        )
        val marcado = original.copy(reemplazadoPor = nuevoNumero)
        val lista = s.protocolos.map { if (it.numero == original.numero) marcado else it } + nuevo
        return s.copy(protocolos = lista) to nuevo
    }

    fun cerrarVisita(s: Sistema, visitaNumero: String, recepcion: Recepcion, usuario: Usuario): Sistema {
        exigir(Reglas.puedeCerrarVisita(usuario), "Solo técnico o ingeniero cierran la visita")
        val visita = s.visita(visitaNumero) ?: throw ReglaNegocioException("La visita no existe")
        exigir(!visita.cerrada, "La visita ya está cerrada")
        val protocolos = s.protocolosDeVisita(visitaNumero)
        exigir(protocolos.any { it.estado != EstadoProtocolo.ANULADO }, "La visita no tiene protocolos")
        val pendientes = Reglas.pendientesCierre(protocolos)
        exigir(pendientes.isEmpty(), "Faltan por registrar: " + pendientes.joinToString(", ") { it.numero } + " (RN-13)")
        exigir(RutValidator.esValido(recepcion.rut), "El RUT de quien recibe no es válido (RN-03)")
        val cerrada = visita.copy(recepcion = recepcion.copy(rut = RutValidator.formatear(recepcion.rut)))
        return s.copy(visitas = s.visitas.map { if (it.numero == visitaNumero) cerrada else it })
    }
}
