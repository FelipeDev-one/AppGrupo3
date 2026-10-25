package com.example.appgrupo3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appgrupo3.data.DatosEjemplo
import com.example.appgrupo3.data.InduztekDataStore
import com.example.appgrupo3.model.Calculos
import com.example.appgrupo3.model.Cliente
import com.example.appgrupo3.model.Equipo
import com.example.appgrupo3.model.EstadoProtocolo
import com.example.appgrupo3.model.Instalacion
import com.example.appgrupo3.model.Instrumento
import com.example.appgrupo3.model.Operaciones
import com.example.appgrupo3.model.Protocolo
import com.example.appgrupo3.model.Prueba
import com.example.appgrupo3.model.Recepcion
import com.example.appgrupo3.model.ReglaNegocioException
import com.example.appgrupo3.model.Resultado
import com.example.appgrupo3.model.Sistema
import com.example.appgrupo3.model.TagDuplicadoException
import com.example.appgrupo3.model.TipoPrueba
import com.example.appgrupo3.model.TipoVisita
import com.example.appgrupo3.model.Usuario
import com.example.appgrupo3.model.Visita
import com.example.appgrupo3.utils.Conectividad
import com.example.appgrupo3.utils.Vibracion
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class Mensaje(val texto: String, val esError: Boolean)

class InduztekViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = InduztekDataStore(application)

    val sistema: StateFlow<Sistema?> = dataStore.obtenerSistema()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val cargando: StateFlow<Boolean> = sistema.map { it == null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val usuario: StateFlow<Usuario?> = combine(sistema, dataStore.obtenerSesion()) { s, rut ->
        s?.usuario(rut)?.takeIf { it.activo }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val conectado: StateFlow<Boolean> = Conectividad.observar(application)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    private val _mensaje = MutableStateFlow<Mensaje?>(null)
    val mensaje: StateFlow<Mensaje?> = _mensaje.asStateFlow()
    private var trabajoMensaje: Job? = null

    init {
        viewModelScope.launch {
            dataStore.inicializarSiFalta { DatosEjemplo.crear() }
        }
    }

    fun hoy(): LocalDate = LocalDate.now()

    fun mostrarMensaje(texto: String, esError: Boolean = false) {
        trabajoMensaje?.cancel()
        _mensaje.value = Mensaje(texto, esError)
        trabajoMensaje = viewModelScope.launch {
            delay(if (esError) 4_500 else 3_000)
            _mensaje.value = null
        }
    }

    private fun <T> ejecutar(
        exito: String?,
        onExito: (T) -> Unit = {},
        onError: (ReglaNegocioException) -> Boolean = { false },
        operacion: (Sistema, Usuario) -> Pair<Sistema, T>
    ) {
        val actual = usuario.value
        if (actual == null) {
            mostrarMensaje("Tu sesión terminó, vuelve a ingresar", true)
            return
        }
        viewModelScope.launch {
            try {
                val resultado = dataStore.actualizar { s -> operacion(s, actual) }
                if (exito != null) mostrarMensaje(exito)
                onExito(resultado)
            } catch (e: ReglaNegocioException) {
                if (!onError(e)) mostrarMensaje(e.message ?: "No se pudo completar la acción", true)
            }
        }
    }

    private fun ejecutarSimple(exito: String?, onExito: () -> Unit = {}, operacion: (Sistema, Usuario) -> Sistema) =
        ejecutar<Unit>(exito, { onExito() }) { s, u -> operacion(s, u) to Unit }

    fun iniciarSesion(rut: String) {
        viewModelScope.launch { dataStore.guardarSesion(rut) }
    }

    fun cerrarSesion() {
        viewModelScope.launch { dataStore.guardarSesion(null) }
    }

    fun restaurarDatos() {
        viewModelScope.launch {
            dataStore.reiniciar { DatosEjemplo.crear(sistema.value?.dispositivo ?: DatosEjemplo.codigoDispositivo()) }
            mostrarMensaje("Se restauraron los datos de ejemplo")
        }
    }
}
