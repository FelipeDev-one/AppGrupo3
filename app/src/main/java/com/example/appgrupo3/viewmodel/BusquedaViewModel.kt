package com.example.appgrupo3.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appgrupo3.model.Categoria
import com.example.appgrupo3.model.Equipo
import com.example.appgrupo3.model.EstadoProtocolo
import com.example.appgrupo3.model.Protocolo
import com.example.appgrupo3.model.Reglas
import com.example.appgrupo3.model.Sistema
import com.example.appgrupo3.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

data class FiltrosUiState(
    val texto: String = "",
    val estado: EstadoProtocolo? = null,
    val clienteRut: String? = null,
    val tecnicoRut: String? = null,
    val soloAtrasados: Boolean = false,
    val categoria: Categoria? = null
)

class BusquedaViewModel : ViewModel() {
    private val _filtros = MutableStateFlow(FiltrosUiState())
    val filtros: StateFlow<FiltrosUiState> = _filtros.asStateFlow()

    fun onTexto(v: String) = _filtros.update { it.copy(texto = v.take(30)) }
    fun onEstado(e: EstadoProtocolo?) = _filtros.update { it.copy(estado = e) }
    fun onCliente(rut: String?) = _filtros.update { it.copy(clienteRut = rut) }
    fun onTecnico(rut: String?) = _filtros.update { it.copy(tecnicoRut = rut) }
    fun onSoloAtrasados(v: Boolean) = _filtros.update { it.copy(soloAtrasados = v) }
    fun onCategoria(c: Categoria?) = _filtros.update { it.copy(categoria = c) }
    fun limpiar() = _filtros.update { FiltrosUiState() }

    private fun coincide(s: Sistema, equipo: Equipo?, texto: String): Boolean {
        if (texto.isBlank()) return true
        if (equipo == null) return false
        val instalacion = s.instalacion(equipo.instalacionId)?.nombre.orEmpty()
        return listOf(equipo.tag, equipo.serie, instalacion).any { it.contains(texto.trim(), ignoreCase = true) }
    }

    private fun coincideEstado(p: Protocolo, estado: EstadoProtocolo?, usuario: Usuario): Boolean {
        if (estado == null) return true
        if (usuario.clienteRut != null && estado == EstadoProtocolo.REGISTRADO) {
            return p.estado == EstadoProtocolo.REGISTRADO || p.estado == EstadoProtocolo.OBSERVADO
        }
        return p.estado == estado
    }

    fun protocolos(s: Sistema, usuario: Usuario, hoy: LocalDate): List<Protocolo> {
        val f = _filtros.value
        return s.protocolos
            .filter { p ->
                val equipo = s.equipo(p.equipoId)
                val cliente = equipo?.let { s.clienteDeEquipo(it) }
                val visibleCliente = usuario.clienteRut == null || (cliente?.rut == usuario.clienteRut && Reglas.visibleParaCliente(p))
                visibleCliente &&
                    coincideEstado(p, f.estado, usuario) &&
                    (f.clienteRut == null || cliente?.rut == f.clienteRut) &&
                    (f.tecnicoRut == null || p.tecnicoRut == f.tecnicoRut) &&
                    (!f.soloAtrasados || Reglas.estaAtrasado(p, hoy)) &&
                    (f.categoria == null || equipo?.tipo?.categoria == f.categoria) &&
                    coincide(s, equipo, f.texto)
            }
            .sortedByDescending { it.fechaHora }
    }

    fun equipos(s: Sistema, usuario: Usuario): List<Equipo> {
        val f = _filtros.value
        return s.equipos
            .filter { e ->
                val cliente = s.clienteDeEquipo(e)
                (usuario.clienteRut == null || cliente?.rut == usuario.clienteRut) &&
                    (f.clienteRut == null || cliente?.rut == f.clienteRut) &&
                    (f.categoria == null || e.tipo.categoria == f.categoria) &&
                    coincide(s, e, f.texto)
            }
            .sortedWith(compareBy({ s.instalacion(it.instalacionId)?.nombre }, { it.tag }))
    }
}
