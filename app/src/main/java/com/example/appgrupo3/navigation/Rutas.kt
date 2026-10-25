package com.example.appgrupo3.navigation

import android.net.Uri

enum class Acceso { PUBLICO, TODOS, INTERNO, JEFE }

sealed class Ruta(val ruta: String, val acceso: Acceso = Acceso.TODOS) {
    data object Login : Ruta("login", Acceso.PUBLICO)
    data object Visitas : Ruta("visitas")
    data object Protocolos : Ruta("protocolos")
    data object Equipos : Ruta("equipos")
    data object Gestion : Ruta("gestion")
    data object NuevaVisita : Ruta("visita_nueva", Acceso.INTERNO)
    data object Clientes : Ruta("clientes", Acceso.INTERNO)
    data object Instrumentos : Ruta("instrumentos", Acceso.INTERNO)
    data object Personal : Ruta("personal", Acceso.JEFE)
    data object PersonalNuevo : Ruta("personal_nuevo", Acceso.JEFE)
    data object Indicadores : Ruta("indicadores", Acceso.INTERNO)

    data object Visita : Ruta("visita/{numero}") {
        fun crear(numero: String) = "visita/${Uri.encode(numero)}"
    }

    data object Protocolo : Ruta("protocolo/{numero}") {
        fun crear(numero: String) = "protocolo/${Uri.encode(numero)}"
    }

    data object Prueba : Ruta("prueba/{numero}/{tipo}", Acceso.INTERNO) {
        fun crear(numero: String, tipo: String) = "prueba/${Uri.encode(numero)}/$tipo"
    }

    data object Historial : Ruta("historial/{equipoId}") {
        fun crear(equipoId: String) = "historial/${Uri.encode(equipoId)}"
    }

    data object EquipoForm : Ruta("equipo_form/{instalacionId}?equipoId={equipoId}&visita={visita}", Acceso.INTERNO) {
        fun crear(instalacionId: String, equipoId: String? = null, visita: String? = null): String {
            val base = "equipo_form/${Uri.encode(instalacionId)}"
            val parametros = listOfNotNull(
                equipoId?.let { "equipoId=${Uri.encode(it)}" },
                visita?.let { "visita=${Uri.encode(it)}" }
            )
            return if (parametros.isEmpty()) base else base + "?" + parametros.joinToString("&")
        }
    }

    data object Recepcion : Ruta("recepcion/{numero}", Acceso.INTERNO) {
        fun crear(numero: String) = "recepcion/${Uri.encode(numero)}"
    }

    data object Acta : Ruta("acta/{numero}") {
        fun crear(numero: String) = "acta/${Uri.encode(numero)}"
    }

    data object ClienteForm : Ruta("cliente_form?rut={rut}", Acceso.INTERNO) {
        fun crear(rut: String? = null) = if (rut == null) "cliente_form" else "cliente_form?rut=${Uri.encode(rut)}"
    }

    data object InstalacionForm : Ruta("instalacion_form/{clienteRut}?id={id}", Acceso.INTERNO) {
        fun crear(clienteRut: String, id: String? = null) =
            "instalacion_form/${Uri.encode(clienteRut)}" + (id?.let { "?id=${Uri.encode(it)}" } ?: "")
    }

    data object InstrumentoForm : Ruta("instrumento_form?serie={serie}", Acceso.JEFE) {
        fun crear(serie: String? = null) = if (serie == null) "instrumento_form" else "instrumento_form?serie=${Uri.encode(serie)}"
    }

    companion object {
        val todas: List<Ruta> by lazy {
            listOf(
                Login, Visitas, Protocolos, Equipos, Gestion, NuevaVisita, Clientes, Instrumentos, Personal,
                PersonalNuevo, Indicadores, Visita, Protocolo, Prueba, Historial, EquipoForm, Recepcion, Acta,
                ClienteForm, InstalacionForm, InstrumentoForm
            )
        }

        fun desdePatron(patron: String?): Ruta? = todas.firstOrNull { it.ruta == patron }
    }
}
