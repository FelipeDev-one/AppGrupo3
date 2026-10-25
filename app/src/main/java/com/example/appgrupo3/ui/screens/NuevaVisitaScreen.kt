package com.example.appgrupo3.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appgrupo3.R
import com.example.appgrupo3.model.TipoVisita
import com.example.appgrupo3.ui.components.BarraConVolver
import com.example.appgrupo3.ui.components.CampoTexto
import com.example.appgrupo3.ui.components.Etiqueta
import com.example.appgrupo3.ui.components.Opcion
import com.example.appgrupo3.ui.components.Selector
import com.example.appgrupo3.ui.components.Tono
import com.example.appgrupo3.ui.theme.Dimens
import com.example.appgrupo3.viewmodel.InduztekViewModel
import com.example.appgrupo3.viewmodel.VisitaFormViewModel

@Composable
fun NuevaVisitaScreen(
    app: InduztekViewModel,
    onVolver: () -> Unit,
    onCreada: (String) -> Unit,
    viewModel: VisitaFormViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val sistema by app.sistema.collectAsStateWithLifecycle()
    val usuario by app.usuario.collectAsStateWithLifecycle()
    val s = sistema ?: return

    val instalaciones = s.instalaciones.filter { it.clienteRut == estado.clienteRut }
    val instalacion = s.instalacion(estado.instalacionId)

    Scaffold(topBar = { BarraConVolver(stringResource(R.string.titulo_nueva_visita), onVolver) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                Modifier.widthIn(max = Dimens.anchoMaximoFormulario),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                Text("Técnico: ${usuario?.nombre ?: ""}", style = MaterialTheme.typography.bodyLarge)
                Selector(
                    etiqueta = "Cliente",
                    icono = Icons.Filled.Business,
                    opciones = s.clientes.map { Opcion(it.rut, it.razonSocial, it.rut) },
                    seleccionado = estado.clienteRut,
                    onSeleccion = viewModel::onCliente,
                    error = estado.errores.cliente
                )
                Selector(
                    etiqueta = "Instalación",
                    icono = Icons.Filled.Factory,
                    opciones = instalaciones.map { Opcion(it.id, it.nombre, it.comuna) },
                    seleccionado = estado.instalacionId,
                    onSeleccion = viewModel::onInstalacion,
                    error = estado.errores.instalacion,
                    habilitado = estado.clienteRut != null,
                    ayuda = if (estado.clienteRut == null) "Primero elige el cliente" else null
                )
                if (instalacion?.sinSenal == true) {
                    Etiqueta("Instalación sin señal: se trabajará sin conexión", Tono.OBSERVADO, Icons.Filled.SignalCellularOff)
                }
                CampoTexto(
                    valor = estado.fecha,
                    onCambio = viewModel::onFecha,
                    etiqueta = "Fecha de la visita",
                    icono = Icons.Filled.CalendarMonth,
                    error = estado.errores.fecha,
                    placeholder = "dd-mm-aaaa",
                    opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text("Tipo de visita", style = MaterialTheme.typography.titleMedium)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    TipoVisita.entries.forEachIndexed { i, tipo ->
                        SegmentedButton(
                            selected = estado.tipo == tipo,
                            onClick = { viewModel.onTipo(tipo) },
                            shape = SegmentedButtonDefaults.itemShape(i, TipoVisita.entries.size)
                        ) { Text(tipo.etiqueta) }
                    }
                }
                Selector(
                    etiqueta = "Ingeniero a cargo",
                    icono = Icons.Filled.Engineering,
                    opciones = s.ingenieros.map { Opcion(it.rut, it.nombre) },
                    seleccionado = estado.ingenieroRut,
                    onSeleccion = viewModel::onIngeniero,
                    error = estado.errores.ingeniero,
                    ayuda = "Formaliza los protocolos aunque no asista"
                )
                Button(
                    onClick = {
                        if (viewModel.validar(app.hoy())) {
                            val e = viewModel.estado.value
                            val fecha = viewModel.fechaValida() ?: return@Button
                            app.crearVisita(e.clienteRut!!, e.instalacionId!!, fecha, e.tipo, e.ingenieroRut!!) { visita ->
                                onCreada(visita.numero)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.alturaBoton)
                ) { Text("Crear visita") }
            }
        }
    }
}
