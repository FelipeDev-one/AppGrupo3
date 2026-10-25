package com.example.appgrupo3.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appgrupo3.model.Instrumento
import com.example.appgrupo3.model.TipoInstrumento
import com.example.appgrupo3.utils.Tiempo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class InstrumentoUiState(
    val esNuevo: Boolean = true,
    val serie: String = "",
    val tipo: TipoInstrumento? = null,
    val nombre: String = "",
    val modelo: String = "",
    val calibracion: String = "",
    val vencimiento: String = "",
    val errorSerie: String? = null,
    val errorTipo: String? = null,
    val errorNombre: String? = null,
    val errorModelo: String? = null,
    val errorCalibracion: String? = null,
    val errorVencimiento: String? = null
)

class InstrumentoFormViewModel : ViewModel() {
    private val _estado = MutableStateFlow(InstrumentoUiState())
    val estado: StateFlow<InstrumentoUiState> = _estado.asStateFlow()
    private var iniciado = false

    fun iniciar(instrumento: Instrumento?) {
        if (iniciado) return
        iniciado = true
        if (instrumento != null) {
            _estado.value = InstrumentoUiState(
                esNuevo = false,
                serie = instrumento.serie,
                tipo = instrumento.tipo,
                nombre = instrumento.nombre,
                modelo = instrumento.modelo,
                calibracion = Tiempo.fechaLegible(instrumento.calibracion),
                vencimiento = Tiempo.fechaLegible(instrumento.vencimiento)
            )
        }
    }

    private fun fecha(v: String) = v.filter { it.isDigit() || it == '-' }.take(10)

    fun onSerie(v: String) = _estado.update { it.copy(serie = v.uppercase().take(20), errorSerie = null) }
    fun onTipo(t: TipoInstrumento) = _estado.update { it.copy(tipo = t, errorTipo = null) }
    fun onNombre(v: String) = _estado.update { it.copy(nombre = v.take(40), errorNombre = null) }
    fun onModelo(v: String) = _estado.update { it.copy(modelo = v.take(40), errorModelo = null) }
    fun onCalibracion(v: String) = _estado.update { it.copy(calibracion = fecha(v), errorCalibracion = null) }
    fun onVencimiento(v: String) = _estado.update { it.copy(vencimiento = fecha(v), errorVencimiento = null) }

    fun validar(): Boolean {
        val e = _estado.value
        val cal = Tiempo.desdeLegible(e.calibracion)
        val ven = Tiempo.desdeLegible(e.vencimiento)
        val nuevo = e.copy(
            errorSerie = if (e.serie.length < 4) "Ingresa el número de serie (mínimo 4 caracteres)" else null,
            errorTipo = if (e.tipo == null) "Selecciona el tipo de instrumento" else null,
            errorNombre = Validaciones.errorNombre(e.nombre, "El nombre"),
            errorModelo = if (e.modelo.isBlank()) "Ingresa marca y modelo" else null,
            errorCalibracion = if (cal == null) "Usa el formato dd-mm-aaaa" else null,
            errorVencimiento = when {
                ven == null -> "Usa el formato dd-mm-aaaa"
                cal != null && !ven.isAfter(cal) -> "Debe ser posterior a la calibración"
                else -> null
            }
        )
        _estado.value = nuevo
        return listOf(nuevo.errorSerie, nuevo.errorTipo, nuevo.errorNombre, nuevo.errorModelo, nuevo.errorCalibracion, nuevo.errorVencimiento)
            .all { it == null }
    }

    fun construir(): Instrumento {
        val e = _estado.value
        return Instrumento(
            serie = e.serie.trim(),
            tipo = e.tipo ?: TipoInstrumento.MEGOHMETRO,
            nombre = e.nombre.trim(),
            modelo = e.modelo.trim(),
            calibracion = Tiempo.desdeLegible(e.calibracion).toString(),
            vencimiento = Tiempo.desdeLegible(e.vencimiento).toString()
        )
    }
}
