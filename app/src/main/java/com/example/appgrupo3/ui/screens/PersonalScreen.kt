package com.example.appgrupo3.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
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
import com.example.appgrupo3.model.Perfil
import com.example.appgrupo3.ui.components.BarraConVolver
import com.example.appgrupo3.ui.components.CampoTexto
import com.example.appgrupo3.ui.components.FormularioScaffold
import com.example.appgrupo3.ui.theme.Dimens
import com.example.appgrupo3.viewmodel.InduztekViewModel
import com.example.appgrupo3.viewmodel.PersonalFormViewModel

@Composable
fun PersonalScreen(app: InduztekViewModel, onVolver: () -> Unit, onNuevo: () -> Unit) {
    val sistema by app.sistema.collectAsStateWithLifecycle()
    val usuario by app.usuario.collectAsStateWithLifecycle()
    val s = sistema ?: return
    val u = usuario ?: return
    val personal = s.usuarios.filter { it.perfil.esInterno }.sortedWith(compareBy({ it.perfil }, { it.nombre }))

    Scaffold(
        topBar = { BarraConVolver(stringResource(R.string.titulo_personal), onVolver) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevo,
                icon = { Icon(Icons.Filled.PersonAdd, contentDescription = null) },
                text = { Text(stringResource(R.string.titulo_personal_nuevo)) }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(Dimens.espacioMedio, Dimens.espacioMedio, Dimens.espacioMedio, Dimens.espacioBajoLista),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(personal, key = { it.rut }) { persona ->
                ListItem(
                    headlineContent = { Text(persona.nombre) },
                    supportingContent = { Text("${persona.perfil.etiqueta} · ${persona.rut}" + if (!persona.activo) " · Desactivado" else "") },
                    leadingContent = { Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = {
                        Switch(
                            checked = persona.activo,
                            onCheckedChange = { app.cambiarActivo(persona.rut, it) },
                            enabled = persona.rut != u.rut
                        )
                    },
                    modifier = Modifier.widthIn(max = Dimens.anchoMaximoLista).fillMaxWidth().animateItem()
                )
            }
        }
    }
}

@Composable
fun PersonalNuevoScreen(app: InduztekViewModel, onVolver: () -> Unit, viewModel: PersonalFormViewModel = viewModel()) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val perfiles = listOf(Perfil.TECNICO, Perfil.INGENIERO, Perfil.JEFE)

    FormularioScaffold(stringResource(R.string.titulo_personal_nuevo), null, onVolver) {
        CampoTexto(
            valor = estado.rut,
            onCambio = viewModel::onRut,
            etiqueta = "RUT",
            icono = Icons.Filled.Badge,
            error = estado.errorRut,
            placeholder = "12.345.678-9",
            opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Ascii)
        )
        CampoTexto(estado.nombre, viewModel::onNombre, "Nombre completo", Icons.Filled.Person, estado.errorNombre)
        Text("Perfil", style = MaterialTheme.typography.titleMedium)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            perfiles.forEachIndexed { i, p ->
                SegmentedButton(
                    selected = estado.perfil == p,
                    onClick = { viewModel.onPerfil(p) },
                    shape = SegmentedButtonDefaults.itemShape(i, perfiles.size)
                ) { Text(if (p == Perfil.INGENIERO) "Ingeniero" else p.etiqueta) }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
            Button(
                onClick = { if (viewModel.validar()) app.guardarPersonal(viewModel.construir(), onVolver) },
                modifier = Modifier.fillMaxWidth().height(Dimens.alturaBoton)
            ) { Text(stringResource(R.string.btn_guardar)) }
        }
    }
}
