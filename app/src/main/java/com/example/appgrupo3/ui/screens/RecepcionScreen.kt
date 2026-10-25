package com.example.appgrupo3.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.example.appgrupo3.model.Acta
import com.example.appgrupo3.model.Calculos
import com.example.appgrupo3.model.Perfil
import com.example.appgrupo3.ui.components.BarraConVolver
import com.example.appgrupo3.ui.components.CampoTexto
import com.example.appgrupo3.ui.components.EtiquetaResultado
import com.example.appgrupo3.ui.components.SeccionTarjeta
import com.example.appgrupo3.ui.theme.Dimens
import com.example.appgrupo3.viewmodel.InduztekViewModel
import com.example.appgrupo3.viewmodel.RecepcionViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecepcionScreen(
    numero: String,
    app: InduztekViewModel,
    onVolver: () -> Unit,
    onCerrada: () -> Unit,
    viewModel: RecepcionViewModel = viewModel()
) {
    val sistema by app.sistema.collectAsStateWithLifecycle()
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val s = sistema ?: return
    val visita = s.visita(numero) ?: return
    val contactos = s.usuarios.filter { it.perfil == Perfil.CLIENTE && it.clienteRut == visita.clienteRut }

    Scaffold(topBar = { BarraConVolver(stringResource(R.string.titulo_recepcion), onVolver, subtitulo = visita.numero) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(Modifier.widthIn(max = Dimens.anchoMaximoFormulario), verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                SeccionTarjeta("Protocolos que irán en el acta", Icons.Filled.Description) {
                    Acta.protocolos(s, visita).forEach { p ->
                        val equipo = s.equipo(p.equipoId)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${p.numero} · ${equipo?.tag}", modifier = Modifier.weight(1f))
                            EtiquetaResultado(equipo?.let { Calculos.global(p, it) })
                        }
                    }
                }
                Text("Datos de quien recibe por el cliente", style = MaterialTheme.typography.titleMedium)
                if (contactos.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                        contactos.forEach { c -> AssistChip(onClick = { viewModel.sugerir(c) }, label = { Text(c.nombre) }) }
                    }
                }
                CampoTexto(estado.nombre, viewModel::onNombre, "Nombre completo", Icons.Filled.Person, estado.errorNombre)
                CampoTexto(
                    valor = estado.rut,
                    onCambio = viewModel::onRut,
                    etiqueta = "RUT",
                    icono = Icons.Filled.Badge,
                    error = estado.errorRut,
                    placeholder = "12.345.678-9",
                    opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Ascii)
                )
                CampoTexto(estado.cargo, viewModel::onCargo, "Cargo", Icons.Filled.Work, estado.errorCargo)
                Text(
                    "La fecha y hora de recepción se registran al confirmar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {
                        if (viewModel.validar()) {
                            app.cerrarVisita(numero, viewModel.construir(System.currentTimeMillis()), onCerrada)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(Dimens.alturaBoton)
                ) { Text("Cerrar visita y generar acta") }
            }
        }
    }
}
