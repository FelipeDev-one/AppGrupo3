package com.example.appgrupo3.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.appgrupo3.R
import com.example.appgrupo3.model.EstadoCalibracion
import com.example.appgrupo3.model.Perfil
import com.example.appgrupo3.model.Reglas
import com.example.appgrupo3.navigation.Ruta
import com.example.appgrupo3.ui.components.DialogoConfirmar
import com.example.appgrupo3.ui.components.FilaDato
import com.example.appgrupo3.ui.components.PanelScaffold
import com.example.appgrupo3.ui.components.SeccionTarjeta
import com.example.appgrupo3.ui.theme.Dimens
import com.example.appgrupo3.viewmodel.InduztekViewModel

private const val MOSTRAR_RESTAURAR_DATOS = false

@Composable
fun GestionScreen(navController: NavController, app: InduztekViewModel) {
    val sistema by app.sistema.collectAsStateWithLifecycle()
    val usuario by app.usuario.collectAsStateWithLifecycle()
    val s = sistema ?: return
    val u = usuario ?: return
    val esCliente = u.perfil == Perfil.CLIENTE
    var confirmarRestaurar by rememberSaveable { mutableStateOf(false) }
    val porVencer = s.instrumentos.count { Reglas.estadoCalibracion(it, app.hoy()) != EstadoCalibracion.VIGENTE }

    PanelScaffold(
        titulo = stringResource(if (esCliente) R.string.titulo_cuenta else R.string.titulo_gestion),
        rutaActual = Ruta.Gestion,
        navController = navController,
        viewModel = app
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioMedio),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(Modifier.widthIn(max = Dimens.anchoMaximoLista), verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                SeccionTarjeta("Sesión", Icons.Filled.Person) {
                    FilaDato("Nombre", u.nombre)
                    FilaDato("RUT", u.rut)
                    FilaDato("Perfil", u.perfil.etiqueta)
                    if (esCliente) {
                        FilaDato("Empresa", s.cliente(u.clienteRut)?.razonSocial ?: "—")
                        FilaDato("Cargo", u.cargo)
                    }
                    FilaDato("Dispositivo", s.dispositivo)
                }

                if (!esCliente) {
                    Column {
                        Opcion("Clientes e instalaciones", "${s.clientes.size} clientes · ${s.instalaciones.size} instalaciones", Icons.Filled.Business) {
                            navController.navigate(Ruta.Clientes.ruta)
                        }
                        Opcion(
                            "Instrumentos",
                            if (porVencer > 0) "$porVencer con calibración vencida o por vencer" else "Todos vigentes",
                            Icons.Filled.Speed
                        ) { navController.navigate(Ruta.Instrumentos.ruta) }
                        Opcion("Indicadores", "Resumen de protocolos y plazos", Icons.Filled.Insights) {
                            navController.navigate(Ruta.Indicadores.ruta)
                        }
                        if (Reglas.puedeGestionarPersonal(u)) {
                            Opcion("Personal Induztek", "Agregar o desactivar técnicos e ingenieros", Icons.Filled.Groups) {
                                navController.navigate(Ruta.Personal.ruta)
                            }
                        }
                    }
                } else {
                    Text(
                        "Tu cuenta es de solo consulta: puedes revisar actas, protocolos y equipos de tu empresa.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                OutlinedButton(onClick = { app.cerrarSesion() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Text("  " + stringResource(R.string.btn_cerrar_sesion))
                }
                if (MOSTRAR_RESTAURAR_DATOS && u.perfil == Perfil.JEFE) {
                    OutlinedButton(onClick = { confirmarRestaurar = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.RestartAlt, contentDescription = null)
                        Text("  Restaurar datos de ejemplo")
                    }
                }
            }
        }
    }

    if (confirmarRestaurar) {
        DialogoConfirmar(
            titulo = "Restaurar datos",
            texto = "Se borrarán los registros nuevos y se cargarán los datos de ejemplo del Anexo 9.",
            textoConfirmar = "Restaurar",
            onConfirmar = {
                confirmarRestaurar = false
                app.restaurarDatos()
            },
            onCancelar = { confirmarRestaurar = false }
        )
    }
}

@Composable
private fun Opcion(titulo: String, detalle: String, icono: ImageVector, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(titulo) },
        supportingContent = { Text(detalle) },
        leadingContent = { Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    )
}
