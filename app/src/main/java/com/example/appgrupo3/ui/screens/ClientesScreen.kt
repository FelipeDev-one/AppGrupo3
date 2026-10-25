package com.example.appgrupo3.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appgrupo3.R
import com.example.appgrupo3.model.Cliente
import com.example.appgrupo3.model.Sistema
import com.example.appgrupo3.ui.components.BarraConVolver
import com.example.appgrupo3.ui.components.Etiqueta
import com.example.appgrupo3.ui.components.Tono
import com.example.appgrupo3.ui.theme.Dimens
import com.example.appgrupo3.viewmodel.InduztekViewModel

@Composable
fun ClientesScreen(
    app: InduztekViewModel,
    onVolver: () -> Unit,
    onNuevoCliente: () -> Unit,
    onEditarCliente: (String) -> Unit,
    onInstalacion: (String, String?) -> Unit,
    onNuevoEquipo: (String) -> Unit
) {
    val sistema by app.sistema.collectAsStateWithLifecycle()
    val s = sistema ?: return

    Scaffold(
        topBar = { BarraConVolver(stringResource(R.string.titulo_clientes), onVolver) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevoCliente,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.titulo_cliente_nuevo)) }
            )
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
            items(s.clientes.sortedBy { it.razonSocial }, key = { it.rut }) { cliente ->
                TarjetaCliente(
                    cliente = cliente,
                    s = s,
                    onEditar = { onEditarCliente(cliente.rut) },
                    onInstalacion = { id -> onInstalacion(cliente.rut, id) },
                    onNuevoEquipo = onNuevoEquipo,
                    modifier = Modifier.widthIn(max = Dimens.anchoMaximoLista).animateItem()
                )
            }
        }
    }
}

@Composable
private fun TarjetaCliente(
    cliente: Cliente,
    s: Sistema,
    onEditar: () -> Unit,
    onInstalacion: (String?) -> Unit,
    onNuevoEquipo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var abierta by rememberSaveable { mutableStateOf(false) }
    val giro by animateFloatAsState(if (abierta) 180f else 0f, label = "giro")
    val instalaciones = s.instalaciones.filter { it.clienteRut == cliente.rut }
    Card(
        onClick = { abierta = !abierta },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            Modifier
                .padding(Dimens.espacioMedio)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(cliente.razonSocial, style = MaterialTheme.typography.titleMedium)
                    Text("${cliente.rut} · ${instalaciones.size} instalación(es)", style = MaterialTheme.typography.bodyMedium)
                }
                Icon(Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.rotate(giro))
            }
            AnimatedVisibility(visible = abierta) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)) {
                    Text("Contacto: ${cliente.contacto} · ${cliente.correo}", style = MaterialTheme.typography.bodyMedium)
                    instalaciones.forEach { inst ->
                        HorizontalDivider()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(inst.nombre, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${inst.comuna} · ${s.equipos.count { it.instalacionId == inst.id }} equipo(s)",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (inst.sinSenal) Etiqueta("Sin señal", Tono.OBSERVADO, Icons.Filled.SignalCellularOff)
                            }
                            IconButton(onClick = { onNuevoEquipo(inst.id) }) {
                                Icon(Icons.Filled.ElectricalServices, contentDescription = "Agregar equipo")
                            }
                            IconButton(onClick = { onInstalacion(inst.id) }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Editar instalación")
                            }
                        }
                    }
                    Row {
                        TextButton(onClick = { onInstalacion(null) }) {
                            Icon(Icons.Filled.AddBusiness, contentDescription = null)
                            Text("  Agregar instalación")
                        }
                        TextButton(onClick = onEditar) {
                            Icon(Icons.Filled.Edit, contentDescription = null)
                            Text("  Editar cliente")
                        }
                    }
                }
            }
        }
    }
}
