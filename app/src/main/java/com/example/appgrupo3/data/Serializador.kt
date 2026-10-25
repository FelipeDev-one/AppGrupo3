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
}
