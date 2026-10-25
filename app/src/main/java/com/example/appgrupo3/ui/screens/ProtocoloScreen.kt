package com.example.appgrupo3.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appgrupo3.model.Calculos
import com.example.appgrupo3.model.ComparacionPrueba
import com.example.appgrupo3.model.EstadoCalibracion
import com.example.appgrupo3.model.EstadoProtocolo
import com.example.appgrupo3.model.Perfil
import com.example.appgrupo3.model.Protocolo
import com.example.appgrupo3.model.Reglas
import com.example.appgrupo3.model.Resultado
import com.example.appgrupo3.model.Sistema
import com.example.appgrupo3.model.TipoPrueba
import com.example.appgrupo3.model.TipoVisita
import com.example.appgrupo3.model.Usuario
import com.example.appgrupo3.ui.components.BannerConexion
import com.example.appgrupo3.ui.components.BarraConVolver
import com.example.appgrupo3.ui.components.CampoTexto
import com.example.appgrupo3.ui.components.DialogoConfirmar
import com.example.appgrupo3.ui.components.DialogoMotivo
import com.example.appgrupo3.ui.components.Etiqueta
import com.example.appgrupo3.ui.components.EtiquetaEstado
import com.example.appgrupo3.ui.components.EtiquetaResultado
import com.example.appgrupo3.ui.components.FilaDato
import com.example.appgrupo3.ui.components.SeccionTarjeta
import com.example.appgrupo3.ui.components.Tono
import com.example.appgrupo3.ui.components.colores
import com.example.appgrupo3.ui.components.tonoDe
import com.example.appgrupo3.ui.theme.CifraResultado
import com.example.appgrupo3.ui.theme.Dimens
import com.example.appgrupo3.utils.Formato
import com.example.appgrupo3.utils.Tiempo
import com.example.appgrupo3.viewmodel.InduztekViewModel
import com.example.appgrupo3.viewmodel.ProtocoloViewModel

private enum class Dialogo { OBSERVAR, ANULAR, FORMALIZAR, REEMPLAZAR }

@Composable
fun ProtocoloScreen(
    numero: String,
    app: InduztekViewModel,
    onVolver: () -> Unit,
    onPrueba: (TipoPrueba) -> Unit,
    onHistorial: (String) -> Unit,
    onProtocolo: (String) -> Unit,
    viewModel: ProtocoloViewModel = viewModel()
) {
    val sistema by app.sistema.collectAsStateWithLifecycle()
    val usuario by app.usuario.collectAsStateWithLifecycle()
    val conectado by app.conectado.collectAsStateWithLifecycle()
    val datos by viewModel.estado.collectAsStateWithLifecycle()
    var dialogo by rememberSaveable { mutableStateOf<Dialogo?>(null) }

    val s = sistema ?: return
    val u = usuario ?: return
    val p = s.protocolo(numero) ?: return
    val equipo = s.equipo(p.equipoId) ?: return
    LaunchedEffect(p.numero) { viewModel.iniciar(p) }

    val esCliente = u.perfil == Perfil.CLIENTE
    val editable = Reglas.puedeEditar(p, u)
    val visita = s.visita(p.visitaNumero)
    val global = Calculos.global(p, equipo)
    val anterior = s.protocoloAnterior(p)
    val comparacion = Calculos.comparar(p, anterior, equipo)
    val hoy = app.hoy()

    Scaffold(
        topBar = {
            Column {
                BarraConVolver(equipo.tag, onVolver, subtitulo = p.numero)
                BannerConexion(conectado)
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioMedio),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(Modifier.widthIn(max = Dimens.anchoMaximoLista), verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                Encabezado(p, s, global, esCliente, Reglas.estaAtrasado(p, hoy), onProtocolo)

                SeccionTarjeta("Condiciones y observaciones", Icons.Filled.Thermostat) {
                    if (editable) {
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                            CampoTexto(
                                valor = datos.temperatura,
                                onCambio = viewModel::onTemperatura,
                                etiqueta = "Temperatura",
                                icono = Icons.Filled.Thermostat,
                                error = datos.errorTemperatura,
                                placeholder = "°C",
                                opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            CampoTexto(
                                valor = datos.humedad,
                                onCambio = viewModel::onHumedad,
                                etiqueta = "Humedad",
                                icono = Icons.Filled.WaterDrop,
                                error = datos.errorHumedad,
                                placeholder = "%",
                                opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        CampoTexto(
                            valor = datos.observaciones,
                            onCambio = viewModel::onObservaciones,
                            etiqueta = "Observaciones del equipo",
                            icono = Icons.Filled.Notes,
                            error = null,
                            ayuda = "${datos.observaciones.length}/300",
                            unaLinea = false,
                            lineasMinimas = 2
                        )
                        if (visita?.tipo == TipoVisita.PRUEBAS_Y_MANTENIMIENTO) {
                            CampoTexto(
                                valor = datos.mantenimiento,
                                onCambio = viewModel::onMantenimiento,
                                etiqueta = "Mantenimiento realizado",
                                icono = Icons.Filled.Construction,
                                error = null,
                                ayuda = "Ej.: limpieza de aisladores, reapriete de conexiones",
                                unaLinea = false,
                                lineasMinimas = 2
                            )
                        }
                        AnimatedVisibility(visible = datos.modificado) {
                            FilledTonalButton(
                                onClick = {
                                    if (viewModel.validarDatos()) {
                                        app.guardarDatosGenerales(
                                            p.numero, viewModel.temperatura(), viewModel.humedad(),
                                            datos.observaciones, datos.mantenimiento
                                        ) { viewModel.marcarGuardado() }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Guardar condiciones") }
                        }
                    } else {
                        FilaDato("Temperatura", p.temperatura?.let { "${Formato.numero(it, 1)} °C" } ?: "Sin registrar")
                        FilaDato("Humedad", p.humedad?.let { "${Formato.numero(it, 0)} %" } ?: "Sin registrar")
                        if (p.observaciones.isNotBlank()) FilaDato("Observaciones", p.observaciones)
                        if (p.mantenimiento.isNotBlank()) FilaDato("Mantenimiento", p.mantenimiento)
                    }
                }

                SeccionTarjeta("Pruebas (${equipo.tipo.etiqueta})", Icons.Filled.Science) {
                    p.pruebas.forEach { prueba ->
                        val resultado = Calculos.resultado(prueba, equipo)
                        val faltan = Calculos.faltantes(prueba, equipo)
                        val instrumento = s.instrumento(prueba.instrumentoSerie)
                        val calibracion = instrumento?.let { Reglas.estadoCalibracion(it, Tiempo.aFecha(p.fechaHora)) }
                        Card(
                            onClick = { if (editable) onPrueba(prueba.tipo) },
                            enabled = editable,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                disabledContentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(Dimens.espacioMedio), verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(prueba.tipo.etiqueta, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                    when {
                                        !prueba.realizada -> Etiqueta("No realizada", Tono.NEUTRO)
                                        resultado != null -> EtiquetaResultado(resultado)
                                        else -> Etiqueta("Incompleta", Tono.NEUTRO)
                                    }
                                    if (editable) Icon(Icons.Filled.ChevronRight, contentDescription = "Editar prueba")
                                }
                                if (!prueba.realizada) Text("Motivo: ${prueba.motivoNoRealizada}", style = MaterialTheme.typography.bodyMedium)
                                Calculos.detalle(prueba, equipo).forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
                                if (prueba.realizada && instrumento != null) {
                                    Text(
                                        "Instrumento: ${instrumento.nombre} ${instrumento.serie} (vence ${Tiempo.fechaLegible(instrumento.vencimiento)})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (calibracion == EstadoCalibracion.VENCIDO && !esCliente) {
                                        Etiqueta("Calibración vencida a la fecha de la prueba (RN-11)", Tono.RECHAZADO, Icons.Filled.Warning)
                                    }
                                }
                                if (prueba.valoresPrevios != null) {
                                    Text(
                                        "Medición previa al mantenimiento: ${prueba.resultadoPrevio?.etiqueta ?: "sin resultado"} · vale la posterior",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (faltan.isNotEmpty() && !esCliente) {
                                    Text(
                                        "Falta: " + faltan.joinToString(", "),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                if (prueba.tipo == TipoPrueba.DEVANADOS && prueba.realizada && Reglas.puedeDefinirResultadoManual(p, u)) {
                                    ResultadoManual(prueba.resultadoManual) { r ->
                                        app.definirResultadoManual(p.numero, prueba.tipo, r)
                                    }
                                }
                            }
                        }
                    }
                }

                Comparacion(comparacion, anterior)

                ComentarioIngeniero(p, u, datos.comentario, datos.errorComentario, comparacion.any { it.significativa }, viewModel::onComentario) {
                    if (viewModel.validarComentario()) app.guardarComentarioIngeniero(p.numero, datos.comentario)
                }

                if (!esCliente) {
                    Acciones(
                        p = p,
                        u = u,
                        faltantesRegistro = Reglas.faltantesRegistro(p, equipo),
                        faltantesFormalizacion = Reglas.faltantesFormalizacion(p, equipo, comparacion),
                        datosSinGuardar = datos.modificado,
                        onRegistrar = { app.cambiarEstado(p.numero, EstadoProtocolo.REGISTRADO) },
                        onDialogo = { dialogo = it }
                    )
                }

                OutlinedButton(onClick = { onHistorial(equipo.id) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.History, contentDescription = null)
                    Text("  Historial de ${equipo.tag}")
                }

                if (!esCliente) HistorialEstados(p, s)
            }
        }
    }
}
