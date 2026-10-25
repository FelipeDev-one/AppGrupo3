package com.example.appgrupo3.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.appgrupo3.model.EstadoProtocolo
import com.example.appgrupo3.model.Reglas
import com.example.appgrupo3.model.Resultado
import com.example.appgrupo3.ui.theme.Aprobado
import com.example.appgrupo3.ui.theme.AprobadoFondo
import com.example.appgrupo3.ui.theme.AprobadoFondoOscuro
import com.example.appgrupo3.ui.theme.AprobadoOscuro
import com.example.appgrupo3.ui.theme.Informativo
import com.example.appgrupo3.ui.theme.InformativoFondo
import com.example.appgrupo3.ui.theme.InformativoFondoOscuro
import com.example.appgrupo3.ui.theme.InformativoOscuro
import com.example.appgrupo3.ui.theme.Neutro
import com.example.appgrupo3.ui.theme.NeutroFondo
import com.example.appgrupo3.ui.theme.NeutroFondoOscuro
import com.example.appgrupo3.ui.theme.NeutroOscuro
import com.example.appgrupo3.ui.theme.Observado
import com.example.appgrupo3.ui.theme.ObservadoFondo
import com.example.appgrupo3.ui.theme.ObservadoFondoOscuro
import com.example.appgrupo3.ui.theme.ObservadoOscuro
import com.example.appgrupo3.ui.theme.Rechazado
import com.example.appgrupo3.ui.theme.RechazadoFondo
import com.example.appgrupo3.ui.theme.RechazadoFondoOscuro
import com.example.appgrupo3.ui.theme.RechazadoOscuro

enum class Tono { APROBADO, OBSERVADO, RECHAZADO, NEUTRO, INFORMATIVO }

data class ColoresTono(val texto: Color, val fondo: Color)

@Composable
fun colores(tono: Tono): ColoresTono {
    val oscuro = isSystemInDarkTheme()
    return when (tono) {
        Tono.APROBADO -> if (oscuro) ColoresTono(AprobadoOscuro, AprobadoFondoOscuro) else ColoresTono(Aprobado, AprobadoFondo)
        Tono.OBSERVADO -> if (oscuro) ColoresTono(ObservadoOscuro, ObservadoFondoOscuro) else ColoresTono(Observado, ObservadoFondo)
        Tono.RECHAZADO -> if (oscuro) ColoresTono(RechazadoOscuro, RechazadoFondoOscuro) else ColoresTono(Rechazado, RechazadoFondo)
        Tono.NEUTRO -> if (oscuro) ColoresTono(NeutroOscuro, NeutroFondoOscuro) else ColoresTono(Neutro, NeutroFondo)
        Tono.INFORMATIVO -> if (oscuro) ColoresTono(InformativoOscuro, InformativoFondoOscuro) else ColoresTono(Informativo, InformativoFondo)
    }
}

fun tonoDe(resultado: Resultado?): Tono = when (resultado) {
    Resultado.APROBADO -> Tono.APROBADO
    Resultado.OBSERVADO -> Tono.OBSERVADO
    Resultado.RECHAZADO -> Tono.RECHAZADO
    null -> Tono.NEUTRO
}

fun tonoDe(estado: EstadoProtocolo): Tono = when (estado) {
    EstadoProtocolo.BORRADOR -> Tono.NEUTRO
    EstadoProtocolo.REGISTRADO -> Tono.INFORMATIVO
    EstadoProtocolo.OBSERVADO -> Tono.OBSERVADO
    EstadoProtocolo.FORMALIZADO -> Tono.APROBADO
    EstadoProtocolo.ANULADO -> Tono.RECHAZADO
}

fun iconoDe(resultado: Resultado?): ImageVector = when (resultado) {
    Resultado.APROBADO -> Icons.Filled.CheckCircle
    Resultado.OBSERVADO -> Icons.Filled.Warning
    Resultado.RECHAZADO -> Icons.Filled.Cancel
    null -> Icons.AutoMirrored.Filled.HelpOutline
}

fun iconoDe(estado: EstadoProtocolo): ImageVector = when (estado) {
    EstadoProtocolo.BORRADOR -> Icons.Filled.EditNote
    EstadoProtocolo.REGISTRADO -> Icons.Filled.HourglassTop
    EstadoProtocolo.OBSERVADO -> Icons.Filled.Report
    EstadoProtocolo.FORMALIZADO -> Icons.Filled.Verified
    EstadoProtocolo.ANULADO -> Icons.Filled.Block
}

@Composable
fun Etiqueta(texto: String, tono: Tono, icono: ImageVector? = null, modifier: Modifier = Modifier) {
    val c = colores(tono)
    val fondo by animateColorAsState(c.fondo, tween(400), label = "fondo")
    val frente by animateColorAsState(c.texto, tween(400), label = "frente")
    Row(
        modifier = modifier
            .background(fondo, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (icono != null) Icon(icono, contentDescription = null, tint = frente, modifier = Modifier.size(16.dp))
        Text(texto, color = frente, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun EtiquetaResultado(resultado: Resultado?, modifier: Modifier = Modifier, textoVacio: String = "Sin resultado") {
    Etiqueta(resultado?.etiqueta ?: textoVacio, tonoDe(resultado), iconoDe(resultado), modifier)
}

@Composable
fun EtiquetaEstado(estado: EstadoProtocolo, paraCliente: Boolean = false, modifier: Modifier = Modifier) {
    val pendiente = paraCliente && (estado == EstadoProtocolo.REGISTRADO || estado == EstadoProtocolo.OBSERVADO)
    if (pendiente) {
        Etiqueta(Reglas.PENDIENTE, Tono.INFORMATIVO, Icons.Filled.HourglassTop, modifier)
    } else {
        Etiqueta(estado.etiqueta, tonoDe(estado), iconoDe(estado), modifier)
    }
}
