package com.example.appgrupo3.ui.screens

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appgrupo3.R
import com.example.appgrupo3.model.TipoEquipo
import com.example.appgrupo3.permisos.Permisos
import com.example.appgrupo3.ui.components.BarraConVolver
import com.example.appgrupo3.ui.components.CampoTexto
import com.example.appgrupo3.ui.components.FotoEvidencia
import com.example.appgrupo3.ui.components.MensajeError
import com.example.appgrupo3.ui.components.Opcion
import com.example.appgrupo3.ui.components.Selector
import com.example.appgrupo3.ui.theme.Dimens
import com.example.appgrupo3.viewmodel.EquipoFormViewModel
import com.example.appgrupo3.viewmodel.InduztekViewModel
import kotlinx.coroutines.launch

@Composable
fun EquipoFormScreen(
    instalacionId: String,
    equipoId: String?,
    visitaNumero: String?,
    app: InduztekViewModel,
    onVolver: () -> Unit,
    onProtocolo: (String) -> Unit,
    viewModel: EquipoFormViewModel = viewModel()
) {
    val sistema by app.sistema.collectAsStateWithLifecycle()
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val s = sistema ?: return
    LaunchedEffect(instalacionId, equipoId) { viewModel.iniciar(instalacionId, s.equipo(equipoId)) }
    val instalacion = s.instalacion(instalacionId)

    val contexto = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()
    val textoDenegado = stringResource(R.string.permiso_camara_denegado)
    val textoAjustes = stringResource(R.string.ajustes)
    val textoSinCamara = stringResource(R.string.sin_camara)
    val textoSinGaleria = stringResource(R.string.sin_galeria)

    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        viewModel.onFotoTomada(exito)
    }

    fun abrirCamara() {
        try {
            lanzadorCamara.launch(viewModel.prepararFoto())
        } catch (e: ActivityNotFoundException) {
            viewModel.onCamaraNoDisponible(textoSinCamara)
        }
    }

    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) {
            abrirCamara()
        } else {
            alcance.launch {
                val r = snackbar.showSnackbar(textoDenegado, actionLabel = textoAjustes, duration = SnackbarDuration.Long)
                if (r == SnackbarResult.ActionPerformed) Permisos.abrirAjustes(contexto)
            }
        }
    }

    val lanzadorGaleria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        viewModel.onImagenGaleria(uri)
    }

    fun guardar() {
        if (!viewModel.validar()) return
        val equipo = viewModel.construir()
        app.guardarEquipo(
            equipo = equipo,
            onDuplicado = viewModel::onDuplicado,
            onExito = {
                viewModel.confirmarGuardado()
                if (visitaNumero != null && estado.esNuevo) {
                    app.agregarProtocolo(visitaNumero, equipo.id) { onProtocolo(it.numero) }
                } else {
                    onVolver()
                }
            }
        )
    }

    Scaffold(
        topBar = {
            BarraConVolver(
                stringResource(if (estado.esNuevo) R.string.titulo_equipo_nuevo else R.string.titulo_equipo_editar),
                onVolver,
                subtitulo = instalacion?.nombre
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(Modifier.widthIn(max = Dimens.anchoMaximoFormulario), verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                Text("Instalación: ${instalacion?.nombre ?: "—"}", style = MaterialTheme.typography.bodyLarge)
                CampoTexto(
                    valor = estado.tag,
                    onCambio = viewModel::onTag,
                    etiqueta = "TAG",
                    icono = Icons.Filled.Tag,
                    error = estado.errores.tag,
                    placeholder = "TR-01",
                    ayuda = "Único dentro de la instalación (RN-01)"
                )
                Selector(
                    etiqueta = "Tipo de equipo",
                    icono = Icons.Filled.Category,
                    opciones = TipoEquipo.entries.map { Opcion(it, it.etiqueta, it.categoria.etiqueta) },
                    seleccionado = estado.tipo,
                    onSeleccion = viewModel::onTipo,
                    error = estado.errores.tipo,
                    habilitado = estado.esNuevo
                )
                CampoTexto(estado.marca, viewModel::onMarca, "Marca", Icons.Filled.Factory, estado.errores.marca)
                CampoTexto(estado.serie, viewModel::onSerie, "Número de serie", Icons.Filled.QrCode, estado.errores.serie)
                CampoTexto(
                    valor = estado.anio,
                    onCambio = viewModel::onAnio,
                    etiqueta = "Año de fabricación",
                    icono = Icons.Filled.CalendarMonth,
                    error = estado.errores.anio,
                    opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                CampoTexto(
                    valor = estado.placa,
                    onCambio = viewModel::onPlaca,
                    etiqueta = "Datos de placa",
                    icono = Icons.Filled.ElectricBolt,
                    error = estado.errores.placa,
                    placeholder = "1.500 kVA, 12 / 0,4 kV"
                )
                AnimatedVisibility(visible = estado.tipo?.usaContactos == true) {
                    CampoTexto(
                        valor = estado.contacto,
                        onCambio = viewModel::onContacto,
                        etiqueta = "Máximo de contactos de placa (µΩ)",
                        icono = Icons.Filled.Numbers,
                        error = estado.errores.contacto,
                        opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
                AnimatedVisibility(visible = estado.tipo == TipoEquipo.RELE) {
                    CampoTexto(
                        valor = estado.ajuste,
                        onCambio = viewModel::onAjuste,
                        etiqueta = "Ajuste de corriente (A)",
                        icono = Icons.Filled.Speed,
                        error = estado.errores.ajuste,
                        opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                Text("Foto de la placa", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Evita errores al copiar los datos de placa en terreno.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    FotoEvidencia(estado.fotoRuta, stringResource(R.string.cd_foto_placa, estado.tag))
                }
                MensajeError(estado.errores.foto)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilledTonalButton(
                        onClick = {
                            if (Permisos.tiene(contexto, Permisos.CAMARA)) abrirCamara() else pedirPermiso.launch(Permisos.CAMARA)
                        },
                        modifier = Modifier.weight(1f).height(Dimens.alturaBotonChico)
                    ) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.btn_camara))
                    }
                    FilledTonalButton(
                        onClick = {
                            try {
                                lanzadorGaleria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            } catch (e: ActivityNotFoundException) {
                                viewModel.mostrarErrorFoto(textoSinGaleria)
                            }
                        },
                        modifier = Modifier.weight(1f).height(Dimens.alturaBotonChico)
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.btn_galeria))
                    }
                }
                AnimatedVisibility(visible = estado.fotoRuta != null) {
                    TextButton(onClick = viewModel::quitarFoto, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                        Text("  " + stringResource(R.string.btn_quitar_foto))
                    }
                }
                Button(
                    onClick = { guardar() },
                    modifier = Modifier.fillMaxWidth().height(Dimens.alturaBoton)
                ) {
                    Text(if (visitaNumero != null && estado.esNuevo) "Guardar y crear protocolo" else stringResource(R.string.btn_guardar))
                }
            }
        }
    }

    estado.duplicado?.let { existente ->
        AlertDialog(
            onDismissRequest = { viewModel.onDuplicado(null) },
            title = { Text("TAG ya registrado (RN-01)") },
            text = {
                Text(
                    "En ${instalacion?.nombre} ya existe ${existente.tag}: ${existente.tipo.etiqueta} ${existente.marca}, serie ${existente.serie}. " +
                        "¿Es el mismo equipo?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDuplicado(null)
                    if (visitaNumero != null) app.agregarProtocolo(visitaNumero, existente.id) { onProtocolo(it.numero) } else onVolver()
                }) { Text(if (visitaNumero != null) "Usar el existente" else "Volver") }
            },
            dismissButton = { TextButton(onClick = { viewModel.onDuplicado(null) }) { Text("Corregir TAG") } }
        )
    }
}
