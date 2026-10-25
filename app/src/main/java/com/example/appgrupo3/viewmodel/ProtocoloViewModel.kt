package com.example.appgrupo3.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appgrupo3.model.Protocolo
import com.example.appgrupo3.utils.Formato
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DatosGeneralesUiState(
    val numero: String = "",
    val temperatura: String = "",
    val humedad: String = "",
    val observaciones: String = "",
    val mantenimiento: String = "",
    val comentario: String = "",
    val errorTemperatura: String? = null,
    val errorHumedad: String? = null,
    val errorComentario: String? = null,
    val modificado: Boolean = false
)

class ProtocoloViewModel : ViewModel() {
    private val _estado = MutableStateFlow(DatosGeneralesUiState())
    val estado: StateFlow<DatosGeneralesUiState> = _estado.asStateFlow()

    fun iniciar(p: Protocolo) {
        if (_estado.value.numero == p.numero) return
        _estado.value = DatosGeneralesUiState(
            numero = p.numero,
            temperatura = Formato.aTexto(p.temperatura),
            humedad = Formato.aTexto(p.humedad),
            observaciones = p.observaciones,
            mantenimiento = p.mantenimiento,
            comentario = p.comentarioIngeniero
        )
    }

    private fun numero(v: String, negativo: Boolean) =
        v.filter { it.isDigit() || it == ',' || it == '.' || (negativo && it == '-') }.take(5)

    fun onTemperatura(v: String) = _estado.update { it.copy(temperatura = numero(v, true), errorTemperatura = null, modificado = true) }
    fun onHumedad(v: String) = _estado.update { it.copy(humedad = numero(v, false), errorHumedad = null, modificado = true) }
    fun onObservaciones(v: String) = _estado.update { it.copy(observaciones = v.take(300), modificado = true) }
    fun onMantenimiento(v: String) = _estado.update { it.copy(mantenimiento = v.take(300), modificado = true) }
    fun onComentario(v: String) = _estado.update { it.copy(comentario = v.take(300), errorComentario = null) }

    fun temperatura(): Double? = Formato.aNumero(_estado.value.temperatura)
    fun humedad(): Double? = Formato.aNumero(_estado.value.humedad)

    fun validarDatos(): Boolean {
        val e = _estado.value
        val t = temperatura()
        val h = humedad()
        val errorT = when {
            e.temperatura.isBlank() -> null
            t == null -> "Número inválido"
            t < -20 || t > 60 -> "Entre -20 y 60 °C"
            else -> null
        }
        val errorH = when {
            e.humedad.isBlank() -> null
            h == null -> "Número inválido"
            h < 0 || h > 100 -> "Entre 0 y 100 %"
            else -> null
        }
        _estado.update { it.copy(errorTemperatura = errorT, errorHumedad = errorH) }
        return errorT == null && errorH == null
    }

    fun validarComentario(): Boolean {
        val c = _estado.value.comentario.trim()
        val error = if (c.length < 10) "Escribe un comentario de al menos 10 caracteres" else null
        _estado.update { it.copy(errorComentario = error) }
        return error == null
    }

    fun marcarGuardado() = _estado.update { it.copy(modificado = false) }
}
