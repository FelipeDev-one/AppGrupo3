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
import com.example.appgrupo3.model.TipoEquipo
import com.example.appgrupo3.model.TipoInstrumento
import com.example.appgrupo3.model.TipoPrueba
import com.example.appgrupo3.model.TipoVisita
import com.example.appgrupo3.model.UnidadAislacion
import com.example.appgrupo3.model.Usuario
import com.example.appgrupo3.model.Visita
import org.json.JSONArray
import org.json.JSONObject

object Serializador {

    private fun <T> lista(texto: String?, leer: (JSONObject) -> T): List<T> {
        if (texto.isNullOrBlank()) return emptyList()
        val arreglo = JSONArray(texto)
        return (0 until arreglo.length()).map { leer(arreglo.getJSONObject(it)) }
    }

    private fun <T> texto(elementos: List<T>, escribir: (T) -> JSONObject): String =
        JSONArray().apply { elementos.forEach { put(escribir(it)) } }.toString()

    private fun JSONObject.textoONulo(clave: String): String? = if (isNull(clave) || !has(clave)) null else getString(clave)

    private fun JSONObject.numeroONulo(clave: String): Double? = if (isNull(clave) || !has(clave)) null else getDouble(clave)

    private fun JSONObject.ponerNulo(clave: String, valor: Any?): JSONObject = put(clave, valor ?: JSONObject.NULL)

    private fun mapaATexto(mapa: Map<String, Double>): JSONObject = JSONObject().apply { mapa.forEach { (k, v) -> put(k, v) } }

    private fun mapaDesde(o: JSONObject?): Map<String, Double> {
        if (o == null) return emptyMap()
        return o.keys().asSequence().associateWith { o.getDouble(it) }
    }

    fun clientesATexto(l: List<Cliente>) = texto(l) {
        JSONObject().put("rut", it.rut).put("razonSocial", it.razonSocial).put("contacto", it.contacto).put("correo", it.correo)
    }

    fun clientesDesde(t: String?) = lista(t) {
        Cliente(it.getString("rut"), it.getString("razonSocial"), it.getString("contacto"), it.getString("correo"))
    }

    fun instalacionesATexto(l: List<Instalacion>) = texto(l) {
        JSONObject().put("id", it.id).put("clienteRut", it.clienteRut).put("nombre", it.nombre)
            .put("comuna", it.comuna).put("sinSenal", it.sinSenal)
    }

    fun instalacionesDesde(t: String?) = lista(t) {
        Instalacion(it.getString("id"), it.getString("clienteRut"), it.getString("nombre"), it.getString("comuna"), it.optBoolean("sinSenal"))
    }

    fun equiposATexto(l: List<Equipo>) = texto(l) {
        JSONObject().put("id", it.id).put("instalacionId", it.instalacionId).put("tag", it.tag).put("tipo", it.tipo.name)
            .put("marca", it.marca).put("serie", it.serie).put("anio", it.anio).put("placa", it.placa)
            .ponerNulo("contactoMaximo", it.contactoMaximo).ponerNulo("ajusteRele", it.ajusteRele).ponerNulo("fotoPlaca", it.fotoPlaca)
    }

    fun equiposDesde(t: String?) = lista(t) {
        Equipo(
            id = it.getString("id"),
            instalacionId = it.getString("instalacionId"),
            tag = it.getString("tag"),
            tipo = TipoEquipo.valueOf(it.getString("tipo")),
            marca = it.getString("marca"),
            serie = it.getString("serie"),
            anio = it.getInt("anio"),
            placa = it.getString("placa"),
            contactoMaximo = it.numeroONulo("contactoMaximo"),
            ajusteRele = it.numeroONulo("ajusteRele"),
            fotoPlaca = it.textoONulo("fotoPlaca")
        )
    }

    fun usuariosATexto(l: List<Usuario>) = texto(l) {
        JSONObject().put("rut", it.rut).put("nombre", it.nombre).put("perfil", it.perfil.name).put("cargo", it.cargo)
            .ponerNulo("clienteRut", it.clienteRut).put("activo", it.activo)
    }

    fun usuariosDesde(t: String?) = lista(t) {
        Usuario(
            it.getString("rut"), it.getString("nombre"), Perfil.valueOf(it.getString("perfil")),
            it.optString("cargo"), it.textoONulo("clienteRut"), it.optBoolean("activo", true)
        )
    }

    fun instrumentosATexto(l: List<Instrumento>) = texto(l) {
        JSONObject().put("serie", it.serie).put("tipo", it.tipo.name).put("nombre", it.nombre).put("modelo", it.modelo)
            .put("calibracion", it.calibracion).put("vencimiento", it.vencimiento)
    }

    fun instrumentosDesde(t: String?) = lista(t) {
        Instrumento(
            it.getString("serie"), TipoInstrumento.valueOf(it.getString("tipo")), it.getString("nombre"),
            it.getString("modelo"), it.getString("calibracion"), it.getString("vencimiento")
        )
    }

    fun visitasATexto(l: List<Visita>) = texto(l) { v ->
        JSONObject().put("numero", v.numero).put("clienteRut", v.clienteRut).put("instalacionId", v.instalacionId)
            .put("fecha", v.fecha).put("tipo", v.tipo.name).put("tecnicoRut", v.tecnicoRut).put("ingenieroRut", v.ingenieroRut)
            .ponerNulo("recepcion", v.recepcion?.let {
                JSONObject().put("nombre", it.nombre).put("rut", it.rut).put("cargo", it.cargo).put("fechaHora", it.fechaHora)
            })
    }

    fun visitasDesde(t: String?) = lista(t) { o ->
        Visita(
            numero = o.getString("numero"),
            clienteRut = o.getString("clienteRut"),
            instalacionId = o.getString("instalacionId"),
            fecha = o.getString("fecha"),
            tipo = TipoVisita.valueOf(o.getString("tipo")),
            tecnicoRut = o.getString("tecnicoRut"),
            ingenieroRut = o.getString("ingenieroRut"),
            recepcion = o.optJSONObject("recepcion")?.let {
                Recepcion(it.getString("nombre"), it.getString("rut"), it.getString("cargo"), it.getLong("fechaHora"))
            }
        )
    }

    private fun pruebaAJson(p: Prueba): JSONObject = JSONObject()
        .put("tipo", p.tipo.name).put("realizada", p.realizada).put("motivoNoRealizada", p.motivoNoRealizada)
        .ponerNulo("instrumentoSerie", p.instrumentoSerie).ponerNulo("tensionKv", p.tensionKv).put("unidad", p.unidad.name)
        .put("valores", mapaATexto(p.valores)).put("noOpera", p.noOpera)
        .ponerNulo("resultadoManual", p.resultadoManual?.name)
        .ponerNulo("valoresPrevios", p.valoresPrevios?.let { mapaATexto(it) })
        .ponerNulo("resultadoPrevio", p.resultadoPrevio?.name)

    private fun pruebaDesde(o: JSONObject): Prueba = Prueba(
        tipo = TipoPrueba.valueOf(o.getString("tipo")),
        realizada = o.optBoolean("realizada", true),
        motivoNoRealizada = o.optString("motivoNoRealizada"),
        instrumentoSerie = o.textoONulo("instrumentoSerie"),
        tensionKv = o.numeroONulo("tensionKv"),
        unidad = UnidadAislacion.valueOf(o.optString("unidad", UnidadAislacion.MEGA.name)),
        valores = mapaDesde(o.optJSONObject("valores")),
        noOpera = o.optBoolean("noOpera"),
        resultadoManual = o.textoONulo("resultadoManual")?.let { Resultado.valueOf(it) },
        valoresPrevios = o.optJSONObject("valoresPrevios")?.let { mapaDesde(it) },
        resultadoPrevio = o.textoONulo("resultadoPrevio")?.let { Resultado.valueOf(it) }
    )

    private fun cambioAJson(c: CambioEstado): JSONObject = JSONObject()
        .ponerNulo("desde", c.desde?.name).put("hacia", c.hacia.name).put("rut", c.rut).put("fechaHora", c.fechaHora).put("motivo", c.motivo)

    private fun cambioDesde(o: JSONObject): CambioEstado = CambioEstado(
        o.textoONulo("desde")?.let { EstadoProtocolo.valueOf(it) },
        EstadoProtocolo.valueOf(o.getString("hacia")),
        o.getString("rut"),
        o.getLong("fechaHora"),
        o.optString("motivo")
    )

    fun protocolosATexto(l: List<Protocolo>) = texto(l) { p ->
        JSONObject().put("numero", p.numero).ponerNulo("visitaNumero", p.visitaNumero).put("equipoId", p.equipoId)
            .put("fechaHora", p.fechaHora).put("tecnicoRut", p.tecnicoRut)
            .ponerNulo("temperatura", p.temperatura).ponerNulo("humedad", p.humedad)
            .put("pruebas", JSONArray().apply { p.pruebas.forEach { put(pruebaAJson(it)) } })
            .put("estado", p.estado.name).put("observaciones", p.observaciones).put("mantenimiento", p.mantenimiento)
            .put("motivoObservacion", p.motivoObservacion).put("motivoAnulacion", p.motivoAnulacion)
            .put("comentarioIngeniero", p.comentarioIngeniero)
            .ponerNulo("reemplazaA", p.reemplazaA).ponerNulo("reemplazadoPor", p.reemplazadoPor)
            .put("historial", JSONArray().apply { p.historial.forEach { put(cambioAJson(it)) } })
    }

    fun protocolosDesde(t: String?) = lista(t) { o ->
        val pruebas = o.getJSONArray("pruebas")
        val historial = o.optJSONArray("historial") ?: JSONArray()
        Protocolo(
            numero = o.getString("numero"),
            visitaNumero = o.textoONulo("visitaNumero"),
            equipoId = o.getString("equipoId"),
            fechaHora = o.getLong("fechaHora"),
            tecnicoRut = o.getString("tecnicoRut"),
            temperatura = o.numeroONulo("temperatura"),
            humedad = o.numeroONulo("humedad"),
            pruebas = (0 until pruebas.length()).map { pruebaDesde(pruebas.getJSONObject(it)) },
            estado = EstadoProtocolo.valueOf(o.getString("estado")),
            observaciones = o.optString("observaciones"),
            mantenimiento = o.optString("mantenimiento"),
            motivoObservacion = o.optString("motivoObservacion"),
            motivoAnulacion = o.optString("motivoAnulacion"),
            comentarioIngeniero = o.optString("comentarioIngeniero"),
            reemplazaA = o.textoONulo("reemplazaA"),
            reemplazadoPor = o.textoONulo("reemplazadoPor"),
            historial = (0 until historial.length()).map { cambioDesde(historial.getJSONObject(it)) }
        )
    }
}
