package com.example.appgrupo3.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appgrupo3.model.Calculos
import com.example.appgrupo3.model.Catalogo
import com.example.appgrupo3.model.Equipo
import com.example.appgrupo3.model.Prueba
import com.example.appgrupo3.model.Resultado
import com.example.appgrupo3.model.TipoPrueba
import com.example.appgrupo3.model.UnidadAislacion
import com.example.appgrupo3.utils.Formato
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PruebaUiState(
    val tipo: TipoPrueba = TipoPrueba.AISLACION,
    val realizada: Boolean = true,
    val motivo: String = "",
    val instrumentoSerie: String? = null,
    val tension: String = "",
    val unidad: UnidadAislacion = UnidadAislacion.MEGA,
    val valores: Map<String, String> = emptyMap(),
    val noOpera: Boolean = false,
    val resultadoManual: Resultado? = null,
    val valoresPrevios: Map<String, Double>? = null,
    val resultadoPrevio: Resultado? = null,
    val erroresValores: Map<String, String> = emptyMap(),
    val errorTension: String? = null,
    val errorMotivo: String? = null,
    val errorInstrumento: String? = null
)

class PruebaViewModel : ViewModel() {
    private val _estado = MutableStateFlow(PruebaUiState())
    val estado: StateFlow<PruebaUiState> = _estado.asStateFlow()
    private var iniciado = false

    val motivosRapidos = listOf(
        "Cliente no autorizó desconexión",
        "Término de ventana de corte",
        "Equipo no se pudo desconectar"
    )

    fun iniciar(prueba: Prueba, equipo: Equipo) {
        if (iniciado) return
        iniciado = true
        val campos = Catalogo.campos(prueba.tipo, equipo.tipo)
        val textos = campos.associate { campo ->
            val valor = prueba.valores[campo.clave]
            val mostrado = if (valor != null && campo.esAislacion) valor / prueba.unidad.factor else valor
            campo.clave to Formato.aTexto(mostrado)
        }
        _estado.value = PruebaUiState(
            tipo = prueba.tipo,
            realizada = prueba.realizada,
            motivo = prueba.motivoNoRealizada,
            instrumentoSerie = prueba.instrumentoSerie,
            tension = Formato.aTexto(prueba.tensionKv),
            unidad = prueba.unidad,
            valores = textos,
            noOpera = prueba.noOpera,
            resultadoManual = prueba.resultadoManual,
            valoresPrevios = prueba.valoresPrevios,
            resultadoPrevio = prueba.resultadoPrevio
        )
    }

    fun onRealizada(v: Boolean) = _estado.update { it.copy(realizada = v, errorMotivo = null) }

    fun onMotivo(v: String) = _estado.update { it.copy(motivo = v.take(120), errorMotivo = null) }

    fun onInstrumento(serie: String) = _estado.update { it.copy(instrumentoSerie = serie, errorInstrumento = null) }

    fun onTension(v: String) = _estado.update { it.copy(tension = v.take(6), errorTension = null) }

    fun onUnidad(u: UnidadAislacion) = _estado.update { it.copy(unidad = u) }

    fun onNoOpera(v: Boolean) = _estado.update { it.copy(noOpera = v) }

    fun onResultadoManual(r: Resultado) = _estado.update { it.copy(resultadoManual = r) }

    fun onValor(clave: String, v: String) = _estado.update {
        val limpio = v.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(10)
        it.copy(valores = it.valores + (clave to limpio), erroresValores = it.erroresValores - clave)
    }

    fun valoresNumericos(equipo: Equipo): Map<String, Double> {
        val e = _estado.value
        return Catalogo.campos(e.tipo, equipo.tipo).mapNotNull { campo ->
            val numero = Formato.aNumero(e.valores[campo.clave] ?: "") ?: return@mapNotNull null
            campo.clave to if (campo.esAislacion) numero * e.unidad.factor else numero
        }.toMap()
    }

    fun resultadoVista(equipo: Equipo): Resultado? {
        val e = _estado.value
        if (!e.realizada) return null
        return Calculos.resultadoDeValores(e.tipo, valoresNumericos(equipo), e.noOpera, e.resultadoManual, equipo)
    }

    fun validar(equipo: Equipo): Boolean {
        val e = _estado.value
        if (!e.realizada) {
            val error = when {
                e.motivo.isBlank() -> "El motivo es obligatorio cuando la prueba no se realiza"
                e.motivo.trim().length < 5 -> "Describe el motivo (mínimo 5 caracteres)"
                else -> null
            }
            _estado.update { it.copy(errorMotivo = error) }
            return error == null
        }
        val errores = mutableMapOf<String, String>()
        Catalogo.campos(e.tipo, equipo.tipo).forEach { campo ->
            val texto = e.valores[campo.clave].orEmpty()
            if (texto.isBlank()) return@forEach
            val numero = Formato.aNumero(texto)
            when {
                numero == null -> errores[campo.clave] = "Número inválido"
                campo.clave == "tap" && (numero < 1 || numero > 33 || numero % 1 != 0.0) -> errores[campo.clave] = "Posición entera entre 1 y 33"
                numero <= 0 -> errores[campo.clave] = "Debe ser mayor que 0"
            }
        }
        val tension = Formato.aNumero(e.tension)
        val errorTension = when {
            !Catalogo.usaTension(e.tipo) || e.tension.isBlank() -> null
            tension == null -> "Número inválido"
            tension < 0.25 || tension > 15 -> "La tensión de prueba va de 0,25 a 15 kV"
            else -> null
        }
        _estado.update { it.copy(erroresValores = errores, errorTension = errorTension) }
        return errores.isEmpty() && errorTension == null
    }

    fun construir(equipo: Equipo): Prueba {
        val e = _estado.value
        return Prueba(
            tipo = e.tipo,
            realizada = e.realizada,
            motivoNoRealizada = if (e.realizada) "" else e.motivo.trim(),
            instrumentoSerie = if (e.realizada) e.instrumentoSerie else null,
            tensionKv = if (e.realizada && Catalogo.usaTension(e.tipo)) Formato.aNumero(e.tension) else null,
            unidad = e.unidad,
            valores = if (e.realizada) valoresNumericos(equipo) else emptyMap(),
            noOpera = e.realizada && e.noOpera,
            resultadoManual = e.resultadoManual,
            valoresPrevios = e.valoresPrevios,
            resultadoPrevio = e.resultadoPrevio
        )
    }

    fun moverAPrevia(equipo: Equipo): Boolean {
        val numericos = valoresNumericos(equipo)
        if (numericos.isEmpty()) return false
        val resultado = resultadoVista(equipo)
        _estado.update { e ->
            e.copy(
                valoresPrevios = numericos,
                resultadoPrevio = resultado,
                valores = e.valores.mapValues { (clave, texto) -> if (clave == "ajuste" || clave == "teorica" || clave == "t_esperado") texto else "" },
                noOpera = false
            )
        }
        return true
    }
}
