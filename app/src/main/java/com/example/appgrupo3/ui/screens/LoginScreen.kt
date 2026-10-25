package com.example.appgrupo3.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appgrupo3.R
import com.example.appgrupo3.model.Perfil
import com.example.appgrupo3.ui.components.CampoTexto
import com.example.appgrupo3.ui.theme.Dimens
import com.example.appgrupo3.viewmodel.InduztekViewModel
import com.example.appgrupo3.viewmodel.LoginViewModel

private const val MOSTRAR_USUARIOS_PRUEBA = false

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    app: InduztekViewModel,
    viewModel: LoginViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val sistema by app.sistema.collectAsStateWithLifecycle()
    val escala = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) {
        escala.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }

    fun ingresar() {
        val s = sistema ?: return
        val usuario = viewModel.validar(s) ?: return
        app.iniciarSesion(usuario.rut)
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            Modifier
                .widthIn(max = Dimens.anchoMaximoFormulario)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            Box(
                Modifier
                    .padding(top = Dimens.espacioGrande)
                    .scale(escala.value)
                    .size(96.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(56.dp))
            }
            Text(stringResource(R.string.marca), style = MaterialTheme.typography.headlineMedium)
            Text(
                stringResource(R.string.subtitulo_marca),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            CampoTexto(
                valor = estado.rut,
                onCambio = viewModel::onRutChange,
                etiqueta = "RUT",
                icono = Icons.Filled.Badge,
                error = estado.error,
                placeholder = "12.345.678-9",
                ayuda = "Personal Induztek o usuario cliente",
                opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
                accionesTeclado = KeyboardActions(onDone = { ingresar() })
            )
            Button(
                onClick = { ingresar() },
                enabled = sistema != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.alturaBoton)
            ) {
                Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Text("  " + stringResource(R.string.btn_ingresar))
            }

            if (MOSTRAR_USUARIOS_PRUEBA) {
                val usuarios = sistema?.usuarios?.filter { it.activo }.orEmpty()
                listOf(
                    "Personal Induztek" to usuarios.filter { it.perfil != Perfil.CLIENTE },
                    "Usuarios cliente" to usuarios.filter { it.perfil == Perfil.CLIENTE }
                ).forEach { (grupo, lista) ->
                    Text(grupo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
                    FlowRow(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                    ) {
                        lista.forEach { u ->
                            AssistChip(
                                onClick = { viewModel.elegir(u) },
                                label = { Text("${u.nombre.substringBefore(" ")} · ${u.perfil.etiqueta}") }
                            )
                        }
                    }
                }
            }
        }
    }
}
