package com.example.appgrupo3.data

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.appgrupo3.model.Sistema
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "induztek_protocolos")

class InduztekDataStore(private val context: Context) {
    private val CLIENTES = stringPreferencesKey("clientes")
    private val INSTALACIONES = stringPreferencesKey("instalaciones")
    private val EQUIPOS = stringPreferencesKey("equipos")
    private val USUARIOS = stringPreferencesKey("usuarios")
    private val INSTRUMENTOS = stringPreferencesKey("instrumentos")
    private val VISITAS = stringPreferencesKey("visitas")
    private val PROTOCOLOS = stringPreferencesKey("protocolos")
    private val DISPOSITIVO = stringPreferencesKey("dispositivo")
    private val SESION_RUT = stringPreferencesKey("sesion_rut")

    private fun leer(p: Preferences): Sistema? {
        if (p[USUARIOS] == null) return null
        return Sistema(
            clientes = Serializador.clientesDesde(p[CLIENTES]),
            instalaciones = Serializador.instalacionesDesde(p[INSTALACIONES]),
            equipos = Serializador.equiposDesde(p[EQUIPOS]),
            usuarios = Serializador.usuariosDesde(p[USUARIOS]),
            instrumentos = Serializador.instrumentosDesde(p[INSTRUMENTOS]),
            visitas = Serializador.visitasDesde(p[VISITAS]),
            protocolos = Serializador.protocolosDesde(p[PROTOCOLOS]),
            dispositivo = p[DISPOSITIVO] ?: "A1"
        )
    }

    private fun escribir(p: MutablePreferences, s: Sistema) {
        p[CLIENTES] = Serializador.clientesATexto(s.clientes)
        p[INSTALACIONES] = Serializador.instalacionesATexto(s.instalaciones)
        p[EQUIPOS] = Serializador.equiposATexto(s.equipos)
        p[USUARIOS] = Serializador.usuariosATexto(s.usuarios)
        p[INSTRUMENTOS] = Serializador.instrumentosATexto(s.instrumentos)
        p[VISITAS] = Serializador.visitasATexto(s.visitas)
        p[PROTOCOLOS] = Serializador.protocolosATexto(s.protocolos)
        p[DISPOSITIVO] = s.dispositivo
    }

    fun obtenerSistema(): Flow<Sistema?> = context.dataStore.data.map { leer(it) }

    fun obtenerSesion(): Flow<String?> = context.dataStore.data.map { it[SESION_RUT] }

    suspend fun inicializarSiFalta(crear: () -> Sistema) {
        context.dataStore.edit { p ->
            if (p[USUARIOS] == null) escribir(p, crear())
        }
    }

    suspend fun reiniciar(crear: () -> Sistema) {
        context.dataStore.edit { p ->
            escribir(p, crear())
        }
    }

    suspend fun <T> actualizar(operacion: (Sistema) -> Pair<Sistema, T>): T {
        var resultado: T? = null
        context.dataStore.edit { p ->
            val actual = leer(p) ?: Sistema()
            val (nuevo, valor) = operacion(actual)
            escribir(p, nuevo)
            resultado = valor
        }
        @Suppress("UNCHECKED_CAST")
        return resultado as T
    }

    suspend fun guardarSesion(rut: String?) {
        context.dataStore.edit { p ->
            if (rut == null) p.remove(SESION_RUT) else p[SESION_RUT] = rut
        }
    }
}
