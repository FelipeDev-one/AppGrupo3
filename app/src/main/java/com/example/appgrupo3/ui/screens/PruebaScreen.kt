package com.example.appgrupo3.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appgrupo3.model.Calculos
import com.example.appgrupo3.model.Catalogo
import com.example.appgrupo3.model.EstadoCalibracion
import com.example.appgrupo3.model.Reglas
import com.example.appgrupo3.model.Resultado
import com.example.appgrupo3.model.TipoPrueba
import com.example.appgrupo3.model.TipoVisita
import com.example.appgrupo3.model.UnidadAislacion
import com.example.appgrupo3.ui.components.BarraConVolver
import com.example.appgrupo3.ui.components.CampoTexto
import com.example.appgrupo3.ui.components.EtiquetaResultado
import com.example.appgrupo3.ui.components.Opcion
import com.example.appgrupo3.ui.components.SeccionTarjeta
import com.example.appgrupo3.ui.components.Selector
import com.example.appgrupo3.ui.theme.Dimens
import com.example.appgrupo3.utils.Formato
import com.example.appgrupo3.utils.Tiempo
import com.example.appgrupo3.viewmodel.InduztekViewModel
import com.example.appgrupo3.viewmodel.PruebaViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PruebaScreen(
    numero: String,
    tipo: TipoPrueba,
    app: InduztekViewModel,
    onVolver: () -> Unit,
    viewModel: PruebaViewModel = viewModel()
) {
    val sistema by app.sistema.collectAsStateWithLifecycle()
    val usuario by app.usuario.collectAsStateWithLifecycle()
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val s = sistema ?: return
    val u = usuario ?: return
    val p = s.protocolo(numero) ?: return
    val equipo = s.equipo(p.equipoId) ?: return
    val prueba = p.prueba(tipo) ?: return
    LaunchedEffect(numero, tipo) { viewModel.iniciar(prueba, equipo) }

    val fechaPrueba = Tiempo.aFecha(p.fechaHora)
    val conMantenimiento = s.visita(p.visitaNumero)?.tipo == TipoVisita.PRUEBAS_Y_MANTENIMIENTO
    val vista = viewModel.construir(equipo)
    val resultado = Calculos.resultado(vista, equipo)
    val campos = Catalogo.campos(tipo, equipo.tipo)
    val instrumentos = s.instrumentos.filter { tipo in it.tipo.pruebas }

    Scaffold(topBar = { BarraConVolver(tipo.etiqueta, onVolver, subtitulo = "${equipo.tag} · ${p.numero}") }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioMedio),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(Modifier.widthIn(max = Dimens.anchoMaximoFormulario), verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Prueba realizada", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Switch(checked = estado.realizada, onCheckedChange = viewModel::onRealizada)
                }

                AnimatedVisibility(visible = !estado.realizada) {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                        Text("Indica por qué no se realizó. Aparecerá en el acta y no cuenta para el resultado global.")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                            viewModel.motivosRapidos.forEach { m -> AssistChip(onClick = { viewModel.onMotivo(m) }, label = { Text(m) }) }
                        }
                        CampoTexto(
                            valor = estado.motivo,
                            onCambio = viewModel::onMotivo,
                            etiqueta = "Motivo de no realización",
                            icono = Icons.Filled.EditNote,
                            error = estado.errorMotivo,
                            unaLinea = false,
                            lineasMinimas = 2
                        )
                    }
                }

                AnimatedVisibility(visible = estado.realizada) {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                        Selector(
                            etiqueta = "Instrumento",
                            icono = Icons.Filled.Speed,
                            opciones = instrumentos.map { i ->
                                val cal = Reglas.estadoCalibracion(i, fechaPrueba)
                                Opcion(
                                    valor = i.serie,
                                    texto = "${i.nombre} · ${i.serie}",
                                    detalle = when (cal) {
                                        EstadoCalibracion.VENCIDO -> "Calibración vencida el ${Tiempo.fechaLegible(i.vencimiento)} (RN-11)"
                                        else -> "Vence el ${Tiempo.fechaLegible(i.vencimiento)}"
                                    },
                                    habilitada = cal != EstadoCalibracion.VENCIDO
                                )
                            },
                            seleccionado = estado.instrumentoSerie,
                            onSeleccion = viewModel::onInstrumento,
                            error = estado.errorInstrumento,
                            ayuda = if (instrumentos.none { Reglas.instrumentoUsable(it, tipo, fechaPrueba) })
                                "No hay instrumentos vigentes: el jefe técnico debe actualizar la calibración" else null
                        )

                        if (Catalogo.usaTension(tipo)) {
                            CampoTexto(
                                valor = estado.tension,
                                onCambio = viewModel::onTension,
                                etiqueta = "Tensión de prueba (kV)",
                                icono = Icons.Filled.Bolt,
                                error = estado.errorTension,
                                opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            Text("Unidad de las lecturas", style = MaterialTheme.typography.bodyMedium)
                            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                                UnidadAislacion.entries.forEachIndexed { i, unidad ->
                                    SegmentedButton(
                                        selected = estado.unidad == unidad,
                                        onClick = { viewModel.onUnidad(unidad) },
                                        shape = SegmentedButtonDefaults.itemShape(i, UnidadAislacion.entries.size)
                                    ) { Text(unidad.etiqueta) }
                                }
                            }
                        }

                        if (tipo == TipoPrueba.OPERACION_RELE) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("El relé no operó", modifier = Modifier.weight(1f))
                                Switch(checked = estado.noOpera, onCheckedChange = viewModel::onNoOpera)
                            }
                        }

                        campos.forEach { campo ->
                            val omitido = campo.opcional && estado.noOpera
                            if (!omitido) {
                                val unidad = if (campo.esAislacion) estado.unidad.etiqueta else campo.unidad
                                CampoTexto(
                                    valor = estado.valores[campo.clave].orEmpty(),
                                    onCambio = { viewModel.onValor(campo.clave, it) },
                                    etiqueta = if (unidad.isBlank()) campo.etiqueta else "${campo.etiqueta} ($unidad)",
                                    icono = Icons.Filled.Calculate,
                                    error = estado.erroresValores[campo.clave],
                                    opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                            }
                        }

                        if (tipo == TipoPrueba.CONTACTOS) {
                            Text(
                                "Máximo de placa: ${equipo.contactoMaximo?.let { Formato.numero(it) + " µΩ" } ?: "sin dato, edita el equipo"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        if (tipo == TipoPrueba.DEVANADOS) {
                            if (Reglas.esRevisor(u)) {
                                Text("Resultado (RN-08c, lo define el ingeniero)", style = MaterialTheme.typography.bodyMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                                    Resultado.entries.forEach { r ->
                                        FilterChip(selected = estado.resultadoManual == r, onClick = { viewModel.onResultadoManual(r) }, label = { Text(r.etiqueta) })
                                    }
                                }
                            } else {
                                Text(
                                    "El resultado de esta prueba lo define el ingeniero (RN-08c).",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (estado.realizada) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(Dimens.espacioMedio), verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Resultado calculado", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                AnimatedContent(targetState = resultado, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "resultado") { r ->
                                    EtiquetaResultado(r, textoVacio = "Faltan datos")
                                }
                            }
                            Calculos.detalle(vista, equipo).forEach { Text(it) }
                        }
                    }
                }

                Button(
                    onClick = {
                        if (viewModel.validar(equipo)) app.guardarPrueba(numero, viewModel.construir(equipo), onVolver)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.alturaBoton)
                ) { Text("Guardar prueba") }
            }
        }
    }
}
