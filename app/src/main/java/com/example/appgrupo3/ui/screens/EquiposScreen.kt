package com.example.appgrupo3.ui.screens

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
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appgrupo3.R
import com.example.appgrupo3.model.Calculos
import com.example.appgrupo3.model.Categoria
import com.example.appgrupo3.model.EstadoProtocolo
import com.example.appgrupo3.model.Perfil
import com.example.appgrupo3.model.Reglas
import com.example.appgrupo3.navigation.Ruta
import com.example.appgrupo3.ui.components.CampoTexto
import com.example.appgrupo3.ui.components.EstadoVacio
import com.example.appgrupo3.ui.components.EtiquetaResultado
import com.example.appgrupo3.ui.components.PanelScaffold
import com.example.appgrupo3.ui.theme.Dimens
import com.example.appgrupo3.utils.Tiempo
import com.example.appgrupo3.viewmodel.BusquedaViewModel
import com.example.appgrupo3.viewmodel.InduztekViewModel

@Composable
fun EquiposScreen(
    navController: NavController,
    app: InduztekViewModel,
    viewModel: BusquedaViewModel = viewModel()
) {
    val sistema by app.sistema.collectAsStateWithLifecycle()
    val usuario by app.usuario.collectAsStateWithLifecycle()
    val filtros by viewModel.filtros.collectAsStateWithLifecycle()
    val s = sistema ?: return
    val u = usuario ?: return
    val esCliente = u.perfil == Perfil.CLIENTE
    val equipos = remember(s, u, filtros) { viewModel.equipos(s, u) }

    PanelScaffold(
        titulo = stringResource(R.string.titulo_equipos),
        rutaActual = Ruta.Equipos,
        navController = navController,
        viewModel = app
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
                Column(Modifier.widthIn(max = Dimens.anchoMaximoLista), verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    CampoTexto(
                        valor = filtros.texto,
                        onCambio = viewModel::onTexto,
                        etiqueta = "Buscar por TAG, serie o instalación",
                        icono = Icons.Filled.Search,
                        error = null
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                        FilterChip(selected = filtros.categoria == null, onClick = { viewModel.onCategoria(null) }, label = { Text("Todos") })
                        Categoria.entries.forEach { c ->
                            FilterChip(selected = filtros.categoria == c, onClick = { viewModel.onCategoria(c) }, label = { Text(c.etiqueta) })
                        }
                    }
                }
            }
            if (equipos.isEmpty()) item { EstadoVacio(Icons.Filled.ElectricalServices, "No se encontraron equipos") }
            items(equipos, key = { it.id }) { equipo ->
                val instalacion = s.instalacion(equipo.instalacionId)
                val ultimo = s.historialEquipo(equipo.id).firstOrNull {
                    if (esCliente) Reglas.visibleParaCliente(it) else it.estado != EstadoProtocolo.ANULADO
                }
                Card(
                    onClick = { navController.navigate(Ruta.Historial.crear(equipo.id)) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    modifier = Modifier
                        .widthIn(max = Dimens.anchoMaximoLista)
                        .fillMaxWidth()
                        .animateItem()
                ) {
                    Column(Modifier.padding(Dimens.espacioMedio), verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${equipo.tag} · ${equipo.tipo.etiqueta}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            if (ultimo != null) EtiquetaResultado(Calculos.global(ultimo, equipo))
                        }
                        Text("${s.cliente(instalacion?.clienteRut)?.razonSocial.orEmpty()} · ${instalacion?.nombre.orEmpty()}")
                        Text(
                            "${equipo.marca} · Serie ${equipo.serie} · ${equipo.tipo.categoria.etiqueta}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            ultimo?.let { "Última prueba: ${Tiempo.fecha(it.fechaHora)}" } ?: "Sin pruebas registradas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
