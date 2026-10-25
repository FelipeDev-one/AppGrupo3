package com.example.appgrupo3.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.appgrupo3.R
import com.example.appgrupo3.model.EstadoProtocolo
import com.example.appgrupo3.model.Perfil
import com.example.appgrupo3.model.Reglas
import com.example.appgrupo3.navigation.Ruta
import com.example.appgrupo3.viewmodel.InduztekViewModel

private data class Pestana(
    val ruta: Ruta,
    val titulo: Int,
    val icono: ImageVector
)

private val pestanasInternas = listOf(
    Pestana(Ruta.Visitas, R.string.nav_visitas, Icons.Filled.Assignment),
    Pestana(Ruta.Protocolos, R.string.nav_protocolos, Icons.Filled.Description),
    Pestana(Ruta.Equipos, R.string.nav_equipos, Icons.Filled.ElectricalServices),
    Pestana(Ruta.Gestion, R.string.nav_gestion, Icons.Filled.Settings)
)

private val pestanasCliente = listOf(
    Pestana(Ruta.Visitas, R.string.nav_actas, Icons.Filled.Assignment),
    Pestana(Ruta.Protocolos, R.string.nav_protocolos, Icons.Filled.Description),
    Pestana(Ruta.Equipos, R.string.nav_equipos, Icons.Filled.ElectricalServices),
    Pestana(Ruta.Gestion, R.string.nav_cuenta, Icons.Filled.ManageAccounts)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelScaffold(
    titulo: String,
    rutaActual: Ruta,
    navController: NavController,
    viewModel: InduztekViewModel,
    acciones: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val usuario by viewModel.usuario.collectAsStateWithLifecycle()
    val sistema by viewModel.sistema.collectAsStateWithLifecycle()
    val conectado by viewModel.conectado.collectAsStateWithLifecycle()
    val esCliente = usuario?.perfil == Perfil.CLIENTE
    val pestanas = if (esCliente) pestanasCliente else pestanasInternas

    val pendientes = run {
        val u = usuario ?: return@run 0
        val s = sistema ?: return@run 0
        when {
            Reglas.esRevisor(u) -> s.protocolos.count { it.estado == EstadoProtocolo.REGISTRADO }
            u.perfil == Perfil.TECNICO -> s.protocolos.count { it.tecnicoRut == u.rut && it.estado == EstadoProtocolo.OBSERVADO }
            else -> 0
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(titulo)
                            usuario?.let {
                                Text("${it.nombre} · ${it.perfil.etiqueta}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    },
                    actions = acciones,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                BannerConexion(conectado)
            }
        },
        bottomBar = {
            NavigationBar {
                pestanas.forEach { pestana ->
                    val contador = if (pestana.ruta == Ruta.Protocolos) pendientes else 0
                    NavigationBarItem(
                        selected = rutaActual == pestana.ruta,
                        onClick = {
                            if (rutaActual != pestana.ruta) {
                                navController.navigate(pestana.ruta.ruta) {
                                    popUpTo(Ruta.Visitas.ruta) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            if (contador > 0) {
                                BadgedBox(badge = { Badge { Text("$contador") } }) {
                                    Icon(pestana.icono, contentDescription = null)
                                }
                            } else {
                                Icon(pestana.icono, contentDescription = null)
                            }
                        },
                        label = { Text(stringResource(pestana.titulo)) }
                    )
                }
            }
        },
        floatingActionButton = floatingActionButton
    ) { padding ->
        content(padding)
    }
}
