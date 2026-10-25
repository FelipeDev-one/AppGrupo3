package com.example.appgrupo3.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appgrupo3.R
import com.example.appgrupo3.model.Calculos
import com.example.appgrupo3.model.Catalogo
import com.example.appgrupo3.model.Equipo
import com.example.appgrupo3.model.EstadoProtocolo
import com.example.appgrupo3.model.Perfil
import com.example.appgrupo3.model.Protocolo
import com.example.appgrupo3.model.Reglas
import com.example.appgrupo3.model.TipoEquipo
import com.example.appgrupo3.model.TipoPrueba
import com.example.appgrupo3.ui.components.BarraConVolver
import com.example.appgrupo3.ui.components.EstadoVacio
import com.example.appgrupo3.ui.components.FilaDato
import com.example.appgrupo3.ui.components.FotoEvidencia
import com.example.appgrupo3.ui.components.SeccionTarjeta
import com.example.appgrupo3.ui.components.TarjetaProtocolo
import com.example.appgrupo3.ui.components.colores
import com.example.appgrupo3.ui.components.tonoDe
import com.example.appgrupo3.ui.theme.Dimens
import com.example.appgrupo3.utils.Formato
import com.example.appgrupo3.utils.Tiempo
import com.example.appgrupo3.viewmodel.InduztekViewModel

@Composable
fun HistorialEquipoScreen(
    equipoId: String,
    app: InduztekViewModel,
    onVolver: () -> Unit,
    onProtocolo: (String) -> Unit,
    onEditar: (Equipo) -> Unit
) {
    val sistema by app.sistema.collectAsStateWithLifecycle()
    val usuario by app.usuario.collectAsStateWithLifecycle()
    val s = sistema ?: return
    val u = usuario ?: return
    val equipo = s.equipo(equipoId) ?: return
    val esCliente = u.perfil == Perfil.CLIENTE
    if (esCliente && s.clienteDeEquipo(equipo)?.rut != u.clienteRut) return
    val instalacion = s.instalacion(equipo.instalacionId)
    val historial = s.historialEquipo(equipoId).filter { !esCliente || Reglas.visibleParaCliente(it) }
    val hoy = app.hoy()

    Scaffold(
        topBar = {
            BarraConVolver(stringResource(R.string.titulo_historial), onVolver, subtitulo = equipo.tag) {
                if (Reglas.puedeRegistrarDatos(u)) {
                    IconButton(onClick = { onEditar(equipo) }) { Icon(Icons.Filled.Edit, contentDescription = "Editar equipo") }
                }
            }
        }
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
                SeccionTarjeta("${equipo.tag} · ${equipo.tipo.etiqueta}", Icons.Filled.ElectricalServices) {
                    if (equipo.fotoPlaca != null) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            FotoEvidencia(equipo.fotoPlaca, stringResource(R.string.cd_foto_placa, equipo.tag))
                        }
                    }
                    FilaDato("Cliente", s.cliente(instalacion?.clienteRut)?.razonSocial ?: "—")
                    FilaDato("Instalación", instalacion?.nombre ?: "—")
                    FilaDato("Categoría", equipo.tipo.categoria.etiqueta)
                    FilaDato("Marca / serie", "${equipo.marca} · ${equipo.serie}")
                    FilaDato("Año", equipo.anio.toString())
                    FilaDato("Datos de placa", equipo.placa)
                    equipo.contactoMaximo?.let { FilaDato("Máx. contactos", "${Formato.numero(it)} µΩ") }
                    equipo.ajusteRele?.let { FilaDato("Ajuste relé", "${Formato.numero(it)} A") }
                    FilaDato("Última prueba", historial.firstOrNull()?.let { Tiempo.fecha(it.fechaHora) } ?: "Sin pruebas")
                }

                val serie = historial
                    .filter { it.estado != EstadoProtocolo.ANULADO }
                    .sortedBy { it.fechaHora }
                    .mapNotNull { p -> valorPrincipal(p, equipo)?.let { p to it } }
                if (serie.size >= 2) {
                    SeccionTarjeta(tituloGrafico(equipo), Icons.Filled.BarChart) {
                        GraficoEvolucion(serie, equipo)
                    }
                }

                SeccionTarjeta("Protocolos (${historial.size})", Icons.Filled.History) {
                    if (historial.isEmpty()) EstadoVacio(Icons.Filled.History, "Este equipo aún no tiene protocolos")
                    historial.forEach { p ->
                        TarjetaProtocolo(
                            protocolo = p,
                            equipo = equipo,
                            subtitulo = "Técnico ${s.nombre(p.tecnicoRut)}" +
                                (p.reemplazadoPor?.let { " · Reemplazado por nº $it" } ?: ""),
                            resultado = Calculos.global(p, equipo),
                            atrasado = Reglas.estaAtrasado(p, hoy),
                            paraCliente = esCliente,
                            onClick = { onProtocolo(p.numero) }
                        )
                    }
                }
            }
        }
    }
}

private fun tituloGrafico(equipo: Equipo): String = when (equipo.tipo) {
    TipoEquipo.RELE -> "Evolución del tiempo de operación (ms)"
    else -> "Evolución de la aislación a 1 min (MΩ)"
}

private fun valorPrincipal(p: Protocolo, equipo: Equipo): Double? {
    if (equipo.tipo == TipoEquipo.RELE) return p.prueba(TipoPrueba.OPERACION_RELE)?.valores?.get("t_medido")
    val prueba = p.prueba(TipoPrueba.AISLACION)?.takeIf { it.realizada } ?: return null
    val claves = Catalogo.paresAislacion(equipo.tipo).map { it.claveUnMinuto }
    return claves.mapNotNull { prueba.valores[it] }.minOrNull()
}

@Composable
private fun GraficoEvolucion(serie: List<Pair<Protocolo, Double>>, equipo: Equipo) {
    val progreso = remember { Animatable(0f) }
    LaunchedEffect(serie.size) { progreso.animateTo(1f, tween(900)) }
    val maximo = serie.maxOf { it.second }
    val coloresBarras = serie.map { (p, _) -> colores(tonoDe(Calculos.global(p, equipo))).texto }
    val fondo = MaterialTheme.colorScheme.outlineVariant

    Canvas(
        Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        val ancho = size.width / serie.size
        val barra = ancho * 0.55f
        drawLine(fondo, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 2f)
        serie.forEachIndexed { i, (_, valor) ->
            val alto = (valor / maximo).toFloat() * size.height * 0.95f * progreso.value
            drawRect(
                color = coloresBarras[i],
                topLeft = Offset(i * ancho + (ancho - barra) / 2, size.height - alto),
                size = Size(barra, alto)
            )
        }
    }
    Row(Modifier.fillMaxWidth()) {
        serie.forEach { (p, valor) ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(Formato.numero(valor, 0), style = MaterialTheme.typography.labelLarge)
                Text(Tiempo.fecha(p.fechaHora).takeLast(4), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
