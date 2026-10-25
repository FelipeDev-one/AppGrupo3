package com.example.appgrupo3.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appgrupo3.R
import com.example.appgrupo3.model.EstadoCalibracion
import com.example.appgrupo3.model.Reglas
import com.example.appgrupo3.model.TipoInstrumento
import com.example.appgrupo3.ui.components.BarraConVolver
import com.example.appgrupo3.ui.components.CampoTexto
import com.example.appgrupo3.ui.components.Etiqueta
import com.example.appgrupo3.ui.components.FormularioScaffold
import com.example.appgrupo3.ui.components.Opcion
import com.example.appgrupo3.ui.components.Selector
import com.example.appgrupo3.ui.components.Tono
import com.example.appgrupo3.ui.theme.Dimens
import com.example.appgrupo3.utils.Tiempo
import com.example.appgrupo3.viewmodel.InduztekViewModel
import com.example.appgrupo3.viewmodel.InstrumentoFormViewModel

@Composable
fun InstrumentosScreen(
    app: InduztekViewModel,
    onVolver: () -> Unit,
    onEditar: (String?) -> Unit
) {
    val sistema by app.sistema.collectAsStateWithLifecycle()
    val usuario by app.usuario.collectAsStateWithLifecycle()
    val s = sistema ?: return
    val u = usuario ?: return
    val puedeGestionar = Reglas.puedeGestionarInstrumentos(u)
    val hoy = app.hoy()
    val lista = s.instrumentos.sortedBy { it.vencimiento }

    Scaffold(
        topBar = { BarraConVolver(stringResource(R.string.titulo_instrumentos), onVolver) },
        floatingActionButton = {
            if (puedeGestionar) {
                ExtendedFloatingActionButton(
                    onClick = { onEditar(null) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Nuevo instrumento") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(Dimens.espacioMedio, Dimens.espacioMedio, Dimens.espacioMedio, Dimens.espacioBajoLista),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    if (puedeGestionar) "Toca un instrumento para actualizar su calibración (RN-12)."
                    else "Solo el jefe técnico registra y actualiza instrumentos (RN-12).",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.widthIn(max = Dimens.anchoMaximoLista).fillMaxWidth()
                )
            }
            items(lista, key = { it.serie }) { i ->
                val estado = Reglas.estadoCalibracion(i, hoy)
                Card(
                    onClick = { if (puedeGestionar) onEditar(i.serie) },
                    enabled = puedeGestionar,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        disabledContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.widthIn(max = Dimens.anchoMaximoLista).fillMaxWidth().animateItem()
                ) {
                    Column(Modifier.padding(Dimens.espacioMedio), verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(i.nombre, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            when (estado) {
                                EstadoCalibracion.VIGENTE -> Etiqueta(estado.etiqueta, Tono.APROBADO, Icons.Filled.CheckCircle)
                                EstadoCalibracion.POR_VENCER -> Etiqueta("Por vencer", Tono.OBSERVADO, Icons.Filled.Schedule)
                                EstadoCalibracion.VENCIDO -> Etiqueta("Vencida", Tono.RECHAZADO, Icons.Filled.Warning)
                            }
                        }
                        Text("${i.modelo} · Serie ${i.serie}")
                        Text(
                            "Calibrado ${Tiempo.fechaLegible(i.calibracion)} · vence ${Tiempo.fechaLegible(i.vencimiento)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Sirve para: " + i.tipo.pruebas.joinToString(", ") { it.etiqueta },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
