package com.example.appgrupo3.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.appgrupo3.R
import com.example.appgrupo3.model.Equipo
import com.example.appgrupo3.model.Protocolo
import com.example.appgrupo3.model.Resultado
import com.example.appgrupo3.ui.theme.Dimens
import com.example.appgrupo3.utils.Tiempo

@Composable
fun SeccionTarjeta(
    titulo: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    accion: @Composable () -> Unit = {},
    contenido: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(modifier.fillMaxWidth()) {
        Column(
            Modifier
                .padding(Dimens.espacioMedio)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.size(Dimens.espacioChico))
                Text(titulo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                accion()
            }
            contenido()
        }
    }
}

@Composable
fun FilaDato(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
        Text(etiqueta, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.45f))
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.55f))
    }
}

@Composable
fun BannerConexion(conectado: Boolean) {
    AnimatedVisibility(visible = !conectado, enter = expandVertically(), exit = shrinkVertically()) {
        Surface(color = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.espacioMedio, vertical = Dimens.espacioChico),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                Icon(Icons.Filled.CloudOff, contentDescription = null, modifier = Modifier.size(Dimens.tamanoIcono))
                Text(stringResource(R.string.sin_conexion), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun EstadoVacio(icono: ImageVector, texto: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(Dimens.espacioGrande),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
    ) {
        Icon(icono, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(texto, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun TarjetaProtocolo(
    protocolo: Protocolo,
    equipo: Equipo?,
    subtitulo: String,
    resultado: Resultado?,
    atrasado: Boolean,
    paraCliente: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.padding(Dimens.espacioMedio), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "${equipo?.tag ?: "Equipo"} · ${equipo?.tipo?.etiqueta ?: ""}",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${protocolo.numero} · ${Tiempo.fecha(protocolo.fechaHora)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                EtiquetaResultado(resultado, textoVacio = "Sin resultado")
            }
            Text(subtitulo, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico), verticalAlignment = Alignment.CenterVertically) {
                EtiquetaEstado(protocolo.estado, paraCliente)
                if (atrasado && !paraCliente) Etiqueta("Atrasado", Tono.RECHAZADO, Icons.Filled.Schedule)
                if (protocolo.reemplazadoPor != null) Etiqueta("Reemplazado", Tono.NEUTRO, Icons.Filled.SwapHoriz)
            }
        }
    }
}

@Composable
fun DialogoMotivo(
    titulo: String,
    descripcion: String,
    textoConfirmar: String,
    onConfirmar: (String) -> Unit,
    onCancelar: () -> Unit
) {
    var motivo by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                Text(descripcion)
                CampoTexto(
                    valor = motivo,
                    onCambio = {
                        motivo = it.take(200)
                        error = null
                    },
                    etiqueta = "Motivo",
                    icono = Icons.Filled.Edit,
                    error = error,
                    ayuda = "${motivo.length}/200",
                    unaLinea = false,
                    lineasMinimas = 2
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val limpio = motivo.trim()
                if (limpio.length < 5) error = "El motivo es obligatorio (mínimo 5 caracteres)" else onConfirmar(limpio)
            }) { Text(textoConfirmar) }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text(stringResource(R.string.btn_cancelar)) } }
    )
}

@Composable
fun DialogoConfirmar(
    titulo: String,
    texto: String,
    textoConfirmar: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = { Text(texto) },
        confirmButton = { TextButton(onClick = onConfirmar) { Text(textoConfirmar) } },
        dismissButton = { TextButton(onClick = onCancelar) { Text(stringResource(R.string.btn_cancelar)) } }
    )
}
