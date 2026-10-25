package com.example.appgrupo3.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import com.example.appgrupo3.ui.components.CampoTexto
import com.example.appgrupo3.ui.components.FormularioScaffold
import com.example.appgrupo3.ui.theme.Dimens
import com.example.appgrupo3.viewmodel.ClienteFormViewModel
import com.example.appgrupo3.viewmodel.InduztekViewModel
import com.example.appgrupo3.viewmodel.InstalacionFormViewModel

@Composable
fun ClienteFormScreen(
    rut: String?,
    app: InduztekViewModel,
    onVolver: () -> Unit,
    viewModel: ClienteFormViewModel = viewModel()
) {
    val sistema by app.sistema.collectAsStateWithLifecycle()
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    LaunchedEffect(rut) { viewModel.iniciar(sistema?.cliente(rut)) }

    FormularioScaffold(stringResource(if (estado.esNuevo) R.string.titulo_cliente_nuevo else R.string.titulo_cliente_editar), null, onVolver) {
        CampoTexto(
            valor = estado.rut,
            onCambio = viewModel::onRut,
            etiqueta = "RUT de la empresa",
            icono = Icons.Filled.Badge,
            error = estado.errorRut,
            placeholder = "76.123.456-7",
            habilitado = estado.esNuevo,
            opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Ascii)
        )
        CampoTexto(estado.razonSocial, viewModel::onRazon, "Razón social", Icons.Filled.Business, estado.errorRazon)
        CampoTexto(estado.contacto, viewModel::onContacto, "Contacto", Icons.Filled.Person, estado.errorContacto)
        CampoTexto(
            valor = estado.correo,
            onCambio = viewModel::onCorreo,
            etiqueta = "Correo",
            icono = Icons.Filled.Email,
            error = estado.errorCorreo,
            opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Button(
            onClick = { if (viewModel.validar()) app.guardarCliente(viewModel.construir(), estado.esNuevo, onVolver) },
            modifier = Modifier.fillMaxWidth().height(Dimens.alturaBoton)
        ) { Text(stringResource(R.string.btn_guardar)) }
    }
}

@Composable
fun InstalacionFormScreen(
    clienteRut: String,
    instalacionId: String?,
    app: InduztekViewModel,
    onVolver: () -> Unit,
    viewModel: InstalacionFormViewModel = viewModel()
) {
    val sistema by app.sistema.collectAsStateWithLifecycle()
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    LaunchedEffect(instalacionId) { viewModel.iniciar(clienteRut, sistema?.instalacion(instalacionId)) }

    FormularioScaffold(stringResource(R.string.titulo_instalacion), sistema?.cliente(clienteRut)?.razonSocial, onVolver) {
        CampoTexto(estado.nombre, viewModel::onNombre, "Nombre de la instalación", Icons.Filled.Factory, estado.errorNombre, placeholder = "Planta Lampa")
        CampoTexto(estado.comuna, viewModel::onComuna, "Comuna", Icons.Filled.LocationCity, estado.errorComuna)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Sin señal en terreno", style = MaterialTheme.typography.titleMedium)
                Text("Avisa al técnico que trabajará sin conexión", style = MaterialTheme.typography.bodyMedium)
            }
            Switch(checked = estado.sinSenal, onCheckedChange = viewModel::onSinSenal)
        }
        Button(
            onClick = { if (viewModel.validar()) app.guardarInstalacion(viewModel.construir(), onVolver) },
            modifier = Modifier.fillMaxWidth().height(Dimens.alturaBoton)
        ) { Text(stringResource(R.string.btn_guardar)) }
    }
}
