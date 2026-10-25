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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.appgrupo3.R
import com.example.appgrupo3.model.EstadoProtocolo
import com.example.appgrupo3.model.Perfil
import com.example.appgrupo3.model.Reglas
import com.example.appgrupo3.model.Sistema
import com.example.appgrupo3.model.TipoVisita
import com.example.appgrupo3.model.Visita
import com.example.appgrupo3.navigation.Ruta
import com.example.appgrupo3.ui.components.EstadoVacio
import com.example.appgrupo3.ui.components.Etiqueta
import com.example.appgrupo3.ui.components.PanelScaffold
import com.example.appgrupo3.ui.components.Tono
import com.example.appgrupo3.ui.theme.Dimens
import com.example.appgrupo3.utils.Tiempo
import com.example.appgrupo3.viewmodel.InduztekViewModel

@Composable
fun VisitasScreen(navController: NavController, app: InduztekViewModel) {
    val sistema by app.sistema.collectAsStateWithLifecycle()
    val usuario by app.usuario.collectAsStateWithLifecycle()
    val s = sistema ?: return
    val u = usuario ?: return
    val esCliente = u.perfil == Perfil.CLIENTE
    var filtro by rememberSaveable { mutableIntStateOf(0) }

    val visitas = s.visitas
        .filter { if (esCliente) it.clienteRut == u.clienteRut && it.cerrada else true }
        .filter {
            when {
                esCliente -> true
                filtro == 0 -> !it.cerrada
                filtro == 1 -> it.cerrada
                else -> true
            }
        }
        .sortedWith(compareBy<Visita> { it.cerrada }.thenByDescending { it.fecha }.thenByDescending { it.numero })

    PanelScaffold(
        titulo = stringResource(if (esCliente) R.string.titulo_actas else R.string.titulo_visitas),
        rutaActual = Ruta.Visitas,
        navController = navController,
        viewModel = app,
        floatingActionButton = {
            if (Reglas.puedeCrearProtocolo(u)) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Ruta.NuevaVisita.ruta) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.titulo_nueva_visita)) }
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
            if (!esCliente) {
                item {
                    Row(Modifier.widthIn(max = Dimens.anchoMaximoLista).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                        listOf("En curso", "Cerradas", "Todas").forEachIndexed { i, texto ->
                            FilterChip(selected = filtro == i, onClick = { filtro = i }, label = { Text(texto) })
                        }
                    }
                }
            } else {
                item {
                    Text(
                        "Actas de visitas cerradas de ${s.cliente(u.clienteRut)?.razonSocial ?: ""}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.widthIn(max = Dimens.anchoMaximoLista).fillMaxWidth()
                    )
                }
            }
            if (visitas.isEmpty()) {
                item {
                    EstadoVacio(
                        Icons.Filled.Assignment,
                        if (esCliente) "Aún no hay actas de tu empresa" else "No hay visitas en esta vista"
                    )
                }
            }
            items(visitas, key = { it.numero }) { visita ->
                TarjetaVisita(
                    visita = visita,
                    s = s,
                    onClick = {
                        navController.navigate(
                            if (esCliente) Ruta.Acta.crear(visita.numero) else Ruta.Visita.crear(visita.numero)
                        )
                    },
                    modifier = Modifier
                        .widthIn(max = Dimens.anchoMaximoLista)
                        .animateItem()
                )
            }
        }
    }
}

@Composable
private fun TarjetaVisita(visita: Visita, s: Sistema, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val protocolos = s.protocolosDeVisita(visita.numero).filter { it.estado != EstadoProtocolo.ANULADO }
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.padding(Dimens.espacioMedio), verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(visita.numero, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (visita.cerrada) Etiqueta("Cerrada", Tono.APROBADO, Icons.Filled.CheckCircle)
                else Etiqueta("En curso", Tono.INFORMATIVO, Icons.Filled.PendingActions)
            }
            Text("${s.cliente(visita.clienteRut)?.razonSocial ?: ""} · ${s.instalacion(visita.instalacionId)?.nombre ?: ""}")
            Text(
                "${Tiempo.fechaLegible(visita.fecha)} · ${protocolos.size} protocolo(s) · Ing. ${s.nombre(visita.ingenieroRut)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (visita.tipo == TipoVisita.PRUEBAS_Y_MANTENIMIENTO) {
                Etiqueta(visita.tipo.etiqueta, Tono.NEUTRO, Icons.Filled.Construction)
            }
        }
    }
}
