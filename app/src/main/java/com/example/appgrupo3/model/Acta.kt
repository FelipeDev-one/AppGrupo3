package com.example.appgrupo3.model

import com.example.appgrupo3.utils.Formato
import com.example.appgrupo3.utils.Tiempo

data class SeccionActa(val titulo: String, val lineas: List<String>, val resultado: Resultado? = null)

object Acta {

    fun protocolos(s: Sistema, visita: Visita): List<Protocolo> =
        s.protocolosDeVisita(visita.numero).filter { it.estado != EstadoProtocolo.ANULADO && it.reemplazaA == null }

    private fun valores(prueba: Prueba, equipo: Equipo): String =
        Catalogo.campos(prueba.tipo, equipo.tipo).mapNotNull { campo ->
            val v = prueba.valores[campo.clave] ?: return@mapNotNull null
            "${campo.etiqueta}: ${Formato.numero(v)}${if (campo.unidad.isBlank()) "" else " " + campo.unidad}"
        }.joinToString("; ")

    fun secciones(s: Sistema, visita: Visita): List<SeccionActa> {
        val instalacion = s.instalacion(visita.instalacionId)
        val encabezado = SeccionActa(
            "Visita ${visita.numero}",
            listOf(
                "Cliente: ${s.cliente(visita.clienteRut)?.razonSocial.orEmpty()} (${visita.clienteRut})",
                "Instalación: ${instalacion?.nombre.orEmpty()}, ${instalacion?.comuna.orEmpty()}",
                "Fecha: ${Tiempo.fechaLegible(visita.fecha)} · ${visita.tipo.etiqueta}",
                "Técnico: ${s.nombre(visita.tecnicoRut)}",
                "Ingeniero a cargo: ${s.nombre(visita.ingenieroRut)}"
            )
        )
        val equipos = protocolos(s, visita).mapNotNull { p ->
            val equipo = s.equipo(p.equipoId) ?: return@mapNotNull null
            val lineas = mutableListOf(
                "Protocolo nº ${p.numero}",
                "${equipo.tipo.etiqueta} ${equipo.marca}, serie ${equipo.serie}, ${equipo.placa}",
                "Condiciones: ${p.temperatura?.let { Formato.numero(it, 1) } ?: "—"} °C · ${p.humedad?.let { Formato.numero(it, 0) } ?: "—"} % HR"
            )
            p.pruebas.forEach { prueba ->
                if (!prueba.realizada) {
                    lineas += "${prueba.tipo.etiqueta}: No realizada (${prueba.motivoNoRealizada})"
                } else {
                    val r = Calculos.resultado(prueba, equipo)?.etiqueta ?: "Pendiente de resultado"
                    lineas += "${prueba.tipo.etiqueta}: $r"
                    val medidas = valores(prueba, equipo)
                    if (medidas.isNotBlank()) lineas += "  $medidas"
                    Calculos.detalle(prueba, equipo).forEach { lineas += "  $it" }
                }
            }
            if (p.mantenimiento.isNotBlank()) lineas += "Mantenimiento: ${p.mantenimiento}"
            if (p.observaciones.isNotBlank()) lineas += "Observaciones: ${p.observaciones}"
            SeccionActa("${equipo.tag} · Resultado ${Calculos.global(p, equipo)?.etiqueta ?: "pendiente"}", lineas, Calculos.global(p, equipo))
        }
        val recepcion = visita.recepcion?.let {
            SeccionActa(
                "Recepción",
                listOf("Recibió: ${it.nombre}", "RUT: ${it.rut}", "Cargo: ${it.cargo}", "Fecha y hora: ${Tiempo.fechaHora(it.fechaHora)}")
            )
        }
        return listOfNotNull(encabezado) + equipos + listOfNotNull(recepcion)
    }

    fun texto(s: Sistema, visita: Visita): String = buildString {
        appendLine("INDUZTEK INGENIERÍA SpA")
        appendLine("ACTA DE PRUEBA EN TERRENO")
        appendLine(Reglas.PENDIENTE.uppercase())
        appendLine()
        secciones(s, visita).forEach { seccion ->
            appendLine(seccion.titulo)
            seccion.lineas.forEach { appendLine(it) }
            appendLine()
        }
    }.trim()
}
