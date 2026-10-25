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
import kotlinx.coroutines.flow.onEach
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

    private val _sesionLista = MutableStateFlow(false)
    val sesionLista: StateFlow<Boolean> = _sesionLista.asStateFlow()

    val usuario: StateFlow<Usuario?> = combine(sistema, dataStore.obtenerSesion()) { s, rut ->
        s?.usuario(rut)?.takeIf { it.activo }
    }.onEach { _sesionLista.value = true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

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

    fun crearVisita(
        clienteRut: String,
        instalacionId: String,
        fecha: LocalDate,
        tipo: TipoVisita,
        ingenieroRut: String,
        onExito: (Visita) -> Unit
    ) = ejecutar("Visita creada", onExito) { s, u ->
        Operaciones.crearVisita(s, clienteRut, instalacionId, fecha, tipo, ingenieroRut, u)
    }

    fun agregarProtocolo(visitaNumero: String, equipoId: String, onExito: (Protocolo) -> Unit) =
        ejecutar("Protocolo creado en Borrador", onExito) { s, u ->
            Operaciones.agregarProtocolo(s, visitaNumero, equipoId, u, System.currentTimeMillis())
        }

    fun guardarDatosGenerales(
        numero: String,
        temperatura: Double?,
        humedad: Double?,
        observaciones: String,
        mantenimiento: String,
        onExito: () -> Unit = {}
    ) = ejecutarSimple("Datos del protocolo guardados", onExito) { s, u ->
        Operaciones.guardarDatosGenerales(s, numero, temperatura, humedad, observaciones, mantenimiento, u)
    }

    fun guardarPrueba(numero: String, prueba: Prueba, onExito: () -> Unit) =
        ejecutarSimple("Prueba guardada", onExito) { s, u -> Operaciones.guardarPrueba(s, numero, prueba, u) }

    fun definirResultadoManual(numero: String, tipo: TipoPrueba, resultado: Resultado) =
        ejecutarSimple("Resultado definido: ${resultado.etiqueta}") { s, u ->
            Operaciones.definirResultadoManual(s, numero, tipo, resultado, u)
        }

    fun guardarComentarioIngeniero(numero: String, comentario: String, onExito: () -> Unit = {}) =
        ejecutarSimple("Comentario guardado", onExito) { s, u ->
            Operaciones.guardarComentarioIngeniero(s, numero, comentario, u)
        }

    fun cambiarEstado(numero: String, hacia: EstadoProtocolo, motivo: String = "", onExito: () -> Unit = {}) =
        ejecutar<Sistema>("Protocolo ${hacia.etiqueta.lowercase()}", { nuevo ->
            avisarSiCorresponde(nuevo, numero, hacia)
            onExito()
        }) { s, u ->
            val nuevo = Operaciones.cambiarEstado(s, numero, hacia, motivo, u, System.currentTimeMillis())
            nuevo to nuevo
        }

    private fun avisarSiCorresponde(s: Sistema, numero: String, hacia: EstadoProtocolo) {
        if (hacia != EstadoProtocolo.REGISTRADO) return
        val p = s.protocolo(numero) ?: return
        val equipo = s.equipo(p.equipoId) ?: return
        val rechazado = Calculos.global(p, equipo) == Resultado.RECHAZADO
        val variacion = Calculos.comparar(p, s.protocoloAnterior(p), equipo).any { it.significativa }
        if (rechazado || variacion) {
            Vibracion.avisar(getApplication())
            mostrarMensaje(
                if (rechazado) "Atención: el protocolo quedó Rechazado" else "Atención: hay una variación significativa (RN-09)",
                true
            )
        }
    }

    fun emitirReemplazo(numero: String, onExito: (Protocolo) -> Unit) =
        ejecutar("Se emitió el protocolo de reemplazo", onExito) { s, u ->
            Operaciones.emitirReemplazo(s, numero, u, System.currentTimeMillis())
        }

    fun cerrarVisita(numero: String, recepcion: Recepcion, onExito: () -> Unit) =
        ejecutarSimple("Visita cerrada y acta generada", onExito) { s, u -> Operaciones.cerrarVisita(s, numero, recepcion, u) }

    fun guardarEquipo(
        equipo: Equipo,
        onDuplicado: (Equipo) -> Unit,
        onExito: () -> Unit
    ) = ejecutar<Unit>(
        exito = "Equipo ${equipo.tag} guardado",
        onExito = { onExito() },
        onError = { e ->
            if (e is TagDuplicadoException) {
                onDuplicado(e.existente)
                true
            } else false
        }
    ) { s, u -> Operaciones.guardarEquipo(s, equipo, u) to Unit }

    fun guardarCliente(cliente: Cliente, esNuevo: Boolean, onExito: () -> Unit) =
        ejecutarSimple("Cliente guardado", onExito) { s, u -> Operaciones.guardarCliente(s, cliente, u, esNuevo) }

    fun guardarInstalacion(instalacion: Instalacion, onExito: () -> Unit) =
        ejecutarSimple("Instalación guardada", onExito) { s, u -> Operaciones.guardarInstalacion(s, instalacion, u) }

    fun guardarInstrumento(instrumento: Instrumento, esNuevo: Boolean, onExito: () -> Unit) =
        ejecutarSimple("Instrumento guardado", onExito) { s, u -> Operaciones.guardarInstrumento(s, instrumento, u, esNuevo) }

    fun guardarPersonal(nuevo: Usuario, onExito: () -> Unit) =
        ejecutarSimple("${nuevo.nombre} fue agregado", onExito) { s, u -> Operaciones.guardarPersonal(s, nuevo, u) }

    fun cambiarActivo(rut: String, activo: Boolean) =
        ejecutarSimple(if (activo) "Usuario activado" else "Usuario desactivado") { s, u ->
            Operaciones.cambiarActivo(s, rut, activo, u)
        }
}
