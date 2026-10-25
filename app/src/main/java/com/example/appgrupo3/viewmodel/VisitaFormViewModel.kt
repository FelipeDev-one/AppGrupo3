package com.example.appgrupo3.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appgrupo3.model.TipoVisita
import com.example.appgrupo3.utils.Tiempo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

data class VisitaErrores(
    val cliente: String? = null,
    val instalacion: String? = null,
    val fecha: String? = null,
    val ingeniero: String? = null
) {
    val hayErrores: Boolean get() = listOf(cliente, instalacion, fecha, ingeniero).any { it != null }
}

data class VisitaUiState(
    val clienteRut: String? = null,
    val instalacionId: String? = null,
    val fecha: String = Tiempo.fecha(System.currentTimeMillis()),
    val tipo: TipoVisita = TipoVisita.SOLO_PRUEBAS,
    val ingenieroRut: String? = null,
    val errores: VisitaErrores = VisitaErrores()
)

class VisitaFormViewModel : ViewModel() {
    private val _estado = MutableStateFlow(VisitaUiState())
    val estado: StateFlow<VisitaUiState> = _estado.asStateFlow()

    fun onCliente(rut: String) = _estado.update {
        it.copy(clienteRut = rut, instalacionId = null, errores = it.errores.copy(cliente = null))
    }

    fun onInstalacion(id: String) = _estado.update {
        it.copy(instalacionId = id, errores = it.errores.copy(instalacion = null))
    }

    fun onFecha(valor: String) = _estado.update {
        it.copy(fecha = valor.filter { c -> c.isDigit() || c == '-' }.take(10), errores = it.errores.copy(fecha = null))
    }

    fun onTipo(tipo: TipoVisita) = _estado.update { it.copy(tipo = tipo) }

    fun onIngeniero(rut: String) = _estado.update {
        it.copy(ingenieroRut = rut, errores = it.errores.copy(ingeniero = null))
    }

    fun fechaValida(): LocalDate? = Tiempo.desdeLegible(_estado.value.fecha)

    fun validar(hoy: LocalDate): Boolean {
        val e = _estado.value
        val fecha = Tiempo.desdeLegible(e.fecha)
        val errores = VisitaErrores(
            cliente = if (e.clienteRut == null) "Selecciona el cliente" else null,
            instalacion = if (e.instalacionId == null) "Selecciona la instalación" else null,
            fecha = when {
                fecha == null -> "Usa el formato dd-mm-aaaa"
                fecha.isAfter(hoy) -> "La visita no puede tener fecha futura"
                fecha.isBefore(hoy.minusDays(30)) -> "Solo se registran visitas de los últimos 30 días"
                else -> null
            },
            ingeniero = if (e.ingenieroRut == null) "Toda visita necesita un ingeniero a cargo" else null
        )
        _estado.update { it.copy(errores = errores) }
        return !errores.hayErrores
    }
}
