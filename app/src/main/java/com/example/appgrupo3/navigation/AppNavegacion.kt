package com.example.appgrupo3.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appgrupo3.R
import com.example.appgrupo3.model.Perfil
import com.example.appgrupo3.model.TipoPrueba
import com.example.appgrupo3.ui.components.MensajeFlotante
import com.example.appgrupo3.ui.screens.ActaScreen
import com.example.appgrupo3.ui.screens.ClienteFormScreen
import com.example.appgrupo3.ui.screens.ClientesScreen
import com.example.appgrupo3.ui.screens.EquipoFormScreen
import com.example.appgrupo3.ui.screens.EquiposScreen
import com.example.appgrupo3.ui.screens.GestionScreen
import com.example.appgrupo3.ui.screens.HistorialEquipoScreen
import com.example.appgrupo3.ui.screens.IndicadoresScreen
import com.example.appgrupo3.ui.screens.InstalacionFormScreen
import com.example.appgrupo3.ui.screens.InstrumentoFormScreen
import com.example.appgrupo3.ui.screens.InstrumentosScreen
import com.example.appgrupo3.ui.screens.LoginScreen
import com.example.appgrupo3.ui.screens.NuevaVisitaScreen
import com.example.appgrupo3.ui.screens.PersonalNuevoScreen
import com.example.appgrupo3.ui.screens.PersonalScreen
import com.example.appgrupo3.ui.screens.ProtocoloScreen
import com.example.appgrupo3.ui.screens.ProtocolosScreen
import com.example.appgrupo3.ui.screens.PruebaScreen
import com.example.appgrupo3.ui.screens.RecepcionScreen
import com.example.appgrupo3.ui.screens.VisitaDetalleScreen
import com.example.appgrupo3.ui.screens.VisitasScreen
import com.example.appgrupo3.viewmodel.InduztekViewModel

private fun NavBackStackEntry.texto(nombre: String): String? = arguments?.getString(nombre)

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val app: InduztekViewModel = viewModel()
    val cargando by app.cargando.collectAsStateWithLifecycle()
    val sesionLista by app.sesionLista.collectAsStateWithLifecycle()
    val usuario by app.usuario.collectAsStateWithLifecycle()
    val mensaje by app.mensaje.collectAsStateWithLifecycle()

    if (cargando || !sesionLista) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.primary) {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.marca), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onPrimary)
                CircularProgressIndicator(color = MaterialTheme.colorScheme.secondaryContainer)
                Text(stringResource(R.string.cargando), color = MaterialTheme.colorScheme.onPrimary)
            }
        }
        return
    }

    val inicio = remember { if (usuario == null) Ruta.Login.ruta else Ruta.Visitas.ruta }
    val entrada by navController.currentBackStackEntryAsState()
    val rutaActual = Ruta.desdePatron(entrada?.destination?.route)

    LaunchedEffect(usuario, rutaActual) {
        val ruta = rutaActual ?: return@LaunchedEffect
        val actual = usuario
        when {
            actual == null && ruta.acceso != Acceso.PUBLICO -> navController.navigate(Ruta.Login.ruta) {
                popUpTo(0)
                launchSingleTop = true
            }
            actual != null && ruta == Ruta.Login -> navController.navigate(Ruta.Visitas.ruta) {
                popUpTo(0)
                launchSingleTop = true
            }
            actual != null && ruta.acceso == Acceso.INTERNO && actual.perfil == Perfil.CLIENTE -> {
                app.mostrarMensaje("Tu perfil es de solo consulta (RN-20)", true)
                navController.popBackStack()
            }
            actual != null && ruta.acceso == Acceso.JEFE && actual.perfil != Perfil.JEFE -> {
                app.mostrarMensaje("Solo el jefe técnico puede entrar aquí", true)
                navController.popBackStack()
            }
        }
    }

    val volver: () -> Unit = { navController.navigateUp() }

    Box(Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = inicio,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) + fadeIn(tween(300))
            },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(300)) },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) + fadeOut(tween(300))
            }
        ) {
            composable(Ruta.Login.ruta) { LoginScreen(app) }
            composable(Ruta.Visitas.ruta) { VisitasScreen(navController, app) }
            composable(Ruta.Protocolos.ruta) { ProtocolosScreen(navController, app) }
            composable(Ruta.Equipos.ruta) { EquiposScreen(navController, app) }
            composable(Ruta.Gestion.ruta) { GestionScreen(navController, app) }

            composable(Ruta.NuevaVisita.ruta) {
                NuevaVisitaScreen(app, volver, onCreada = { numero ->
                    navController.navigate(Ruta.Visita.crear(numero)) { popUpTo(Ruta.Visitas.ruta) }
                })
            }
            composable(Ruta.Visita.ruta, listOf(navArgument("numero") { type = NavType.StringType })) { e ->
                val numero = e.texto("numero") ?: return@composable
                VisitaDetalleScreen(
                    numero = numero,
                    app = app,
                    onVolver = volver,
                    onProtocolo = { navController.navigate(Ruta.Protocolo.crear(it)) },
                    onNuevoEquipo = { navController.navigate(Ruta.EquipoForm.crear(it, visita = numero)) },
                    onCerrar = { navController.navigate(Ruta.Recepcion.crear(numero)) },
                    onActa = { navController.navigate(Ruta.Acta.crear(numero)) }
                )
            }
            composable(Ruta.Protocolo.ruta, listOf(navArgument("numero") { type = NavType.StringType })) { e ->
                val numero = e.texto("numero") ?: return@composable
                ProtocoloScreen(
                    numero = numero,
                    app = app,
                    onVolver = volver,
                    onPrueba = { navController.navigate(Ruta.Prueba.crear(numero, it.name)) },
                    onHistorial = { navController.navigate(Ruta.Historial.crear(it)) },
                    onProtocolo = { navController.navigate(Ruta.Protocolo.crear(it)) }
                )
            }
            composable(
                Ruta.Prueba.ruta,
                listOf(navArgument("numero") { type = NavType.StringType }, navArgument("tipo") { type = NavType.StringType })
            ) { e ->
                val numero = e.texto("numero") ?: return@composable
                val tipo = e.texto("tipo")?.let { runCatching { TipoPrueba.valueOf(it) }.getOrNull() } ?: return@composable
                PruebaScreen(numero, tipo, app, volver)
            }
            composable(Ruta.Historial.ruta, listOf(navArgument("equipoId") { type = NavType.StringType })) { e ->
                val id = e.texto("equipoId") ?: return@composable
                HistorialEquipoScreen(
                    equipoId = id,
                    app = app,
                    onVolver = volver,
                    onProtocolo = { navController.navigate(Ruta.Protocolo.crear(it)) },
                    onEditar = { navController.navigate(Ruta.EquipoForm.crear(it.instalacionId, it.id)) }
                )
            }
            composable(
                Ruta.EquipoForm.ruta,
                listOf(
                    navArgument("instalacionId") { type = NavType.StringType },
                    navArgument("equipoId") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("visita") { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) { e ->
                val instalacionId = e.texto("instalacionId") ?: return@composable
                EquipoFormScreen(
                    instalacionId = instalacionId,
                    equipoId = e.texto("equipoId"),
                    visitaNumero = e.texto("visita"),
                    app = app,
                    onVolver = volver,
                    onProtocolo = { numero ->
                        navController.navigate(Ruta.Protocolo.crear(numero)) {
                            popUpTo(Ruta.EquipoForm.ruta) { inclusive = true }
                        }
                    }
                )
            }
            composable(Ruta.Recepcion.ruta, listOf(navArgument("numero") { type = NavType.StringType })) { e ->
                val numero = e.texto("numero") ?: return@composable
                RecepcionScreen(numero, app, volver, onCerrada = {
                    navController.navigate(Ruta.Acta.crear(numero)) {
                        popUpTo(Ruta.Recepcion.ruta) { inclusive = true }
                    }
                })
            }
            composable(Ruta.Acta.ruta, listOf(navArgument("numero") { type = NavType.StringType })) { e ->
                val numero = e.texto("numero") ?: return@composable
                ActaScreen(numero, app, volver)
            }
        }

        MensajeFlotante(
            mensaje = mensaje,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 72.dp)
        )
    }
}
