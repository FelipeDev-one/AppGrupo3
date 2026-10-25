package com.example.appgrupo3.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appgrupo3.R
import com.example.appgrupo3.model.Calculos
import com.example.appgrupo3.model.EstadoCalibracion
import com.example.appgrupo3.model.EstadoProtocolo
import com.example.appgrupo3.model.Reglas
import com.example.appgrupo3.model.Resultado
import com.example.appgrupo3.ui.components.BarraConVolver
import com.example.appgrupo3.ui.components.FilaDato
import com.example.appgrupo3.ui.components.SeccionTarjeta
import com.example.appgrupo3.ui.components.colores
import com.example.appgrupo3.ui.components.tonoDe
import com.example.appgrupo3.ui.theme.Dimens
import com.example.appgrupo3.utils.Tiempo
import com.example.appgrupo3.viewmodel.InduztekViewModel

@Composable
fun IndicadoresScreen(app: InduztekViewModel, onVolver: () -> Unit, onProtocolo: (String) -> Unit) {
    val sistema by app.sistema.collectAsStateWithLifecycle()
    val s = sistema ?: return
    val hoy = app.hoy()
    val vigentes = s.protocolos.filter { it.estado != EstadoProtocolo.ANULADO }
    val atrasados = vigentes.filter { Reglas.estaAtrasado(it, hoy) }
    val resultados = vigentes.mapNotNull { p -> s.equipo(p.equipoId)?.let { Calculos.global(p, it) } }

    Scaffold(topBar = { BarraConVolver(stringResource(R.string.titulo_indicadores), onVolver) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioMedio),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(Modifier.widthIn(max = Dimens.anchoMaximoLista), verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    Cifra("Protocolos", vigentes.size.toString(), Modifier.weight(1f))
                    Cifra("Por formalizar", vigentes.count { it.estado == EstadoProtocolo.REGISTRADO }.toString(), Modifier.weight(1f))
                    Cifra("Atrasados", atrasados.size.toString(), Modifier.weight(1f))
                }

                SeccionTarjeta("Resultados globales", Icons.Filled.Assessment) {
                    Resultado.entries.forEach { r ->
                        val cantidad = resultados.count { it == r }
                        Barra(r.etiqueta, cantidad, resultados.size.coerceAtLeast(1), colores(tonoDe(r)).texto)
                    }
                }

                SeccionTarjeta("Estados", Icons.Filled.Assessment) {
                    EstadoProtocolo.entries.forEach { e ->
                        FilaDato(e.etiqueta, s.protocolos.count { it.estado == e }.toString())
                    }
                }

                SeccionTarjeta("Atrasados: más de ${Reglas.DIAS_HABILES_FORMALIZACION} días hábiles (RN-16)", Icons.Filled.Schedule) {
                    if (atrasados.isEmpty()) Text("No hay protocolos atrasados.")
                    atrasados.sortedBy { it.fechaHora }.forEach { p ->
                        Card(onClick = { onProtocolo(p.numero) }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(Dimens.espacioChico)) {
                                Text("${p.numero} · ${s.equipo(p.equipoId)?.tag}", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${p.estado.etiqueta} · ${Reglas.diasHabilesDesdePrueba(p, hoy)} días hábiles desde ${Tiempo.fecha(p.fechaHora)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                SeccionTarjeta("Protocolos por técnico", Icons.Filled.Engineering) {
                    s.tecnicos.forEach { t ->
                        val n = vigentes.count { it.tecnicoRut == t.rut }
                        if (n > 0) FilaDato(t.nombre, n.toString())
                    }
                }

                SeccionTarjeta("Instrumentos", Icons.Filled.Speed) {
                    EstadoCalibracion.entries.forEach { e ->
                        FilaDato(e.etiqueta, s.instrumentos.count { Reglas.estadoCalibracion(it, hoy) == e }.toString())
                    }
                }
            }
        }
    }
}

@Composable
private fun Cifra(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(Dimens.espacioMedio).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(valor, style = MaterialTheme.typography.headlineMedium)
            Text(titulo, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun Barra(etiqueta: String, cantidad: Int, total: Int, color: Color) {
    val fraccion by animateFloatAsState(cantidad.toFloat() / total, tween(800), label = "barra")
    Column {
        Text("$etiqueta · $cantidad", style = MaterialTheme.typography.bodyMedium)
        Box(
            Modifier
                .fillMaxWidth()
                .height(14.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(7.dp))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraccion)
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(7.dp))
            )
        }
    }
}
