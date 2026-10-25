package com.example.appgrupo3.ui.screens

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
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appgrupo3.R
import com.example.appgrupo3.model.Acta
import com.example.appgrupo3.ui.components.EtiquetaResultado
import com.example.appgrupo3.ui.theme.Dimens
import com.example.appgrupo3.ui.components.BarraConVolver
import com.example.appgrupo3.utils.Compartir
import com.example.appgrupo3.viewmodel.InduztekViewModel

@Composable
fun ActaScreen(numero: String, app: InduztekViewModel, onVolver: () -> Unit) {
    val sistema by app.sistema.collectAsStateWithLifecycle()
    val usuario by app.usuario.collectAsStateWithLifecycle()
    val s = sistema ?: return
    val u = usuario ?: return
    val visita = s.visita(numero) ?: return
    if (u.clienteRut != null && u.clienteRut != visita.clienteRut) return
    val contexto = LocalContext.current
    val secciones = Acta.secciones(s, visita)

    fun compartir() {
        val ok = Compartir.texto(contexto, "Acta ${visita.numero} · Induztek", Acta.texto(s, visita))
        if (!ok) app.mostrarMensaje("No hay apps para compartir el acta", true)
    }

    Scaffold(
        topBar = {
            BarraConVolver(stringResource(R.string.titulo_acta), onVolver, subtitulo = visita.numero) {
                IconButton(onClick = { compartir() }) { Icon(Icons.Filled.Share, contentDescription = "Compartir acta") }
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
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(Dimens.espacioMedio), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.HourglassTop, contentDescription = null)
                        Column(Modifier.padding(start = Dimens.espacioChico)) {
                            Text(stringResource(R.string.pendiente_formalizacion).uppercase(), style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Respaldo entregado en terreno. Los protocolos serán formalizados por el ingeniero a cargo.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                secciones.forEach { seccion ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(Dimens.espacioMedio), verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(seccion.titulo.substringBefore(" · "), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                if (seccion.titulo.contains(" · Resultado")) EtiquetaResultado(seccion.resultado, textoVacio = "Pendiente")
                            }
                            seccion.lineas.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
                        }
                    }
                }
                Button(onClick = { compartir() }, modifier = Modifier.fillMaxWidth().height(Dimens.alturaBoton)) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Text("  Compartir acta con el cliente")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "  Sin señal, el acta queda guardada y se comparte apenas haya conexión (RN-15).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
