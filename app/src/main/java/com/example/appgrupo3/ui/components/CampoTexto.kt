package com.example.appgrupo3.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import com.example.appgrupo3.ui.theme.Dimens



@Composable
fun CampoTexto(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    error: String?,
    modifier: Modifier = Modifier,
    ayuda: String? = null,
    placeholder: String? = null,
    prefijo: String? = null,
    habilitado: Boolean = true,
    unaLinea: Boolean = true,
    lineasMinimas: Int = 1,
    estiloTexto: TextStyle? = null,
    opcionesTeclado: KeyboardOptions = KeyboardOptions.Default,
    accionesTeclado: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        placeholder = placeholder?.let { texto -> { Text(texto) } },
        prefix = prefijo?.let { texto -> { Text(texto) } },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = if (error != null) {
            { Icon(Icons.Filled.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error) }
        } else null,
        isError = error != null,
        supportingText = (error ?: ayuda)?.let { texto ->
            {
                Text(
                    texto,
                    color = if (error != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        enabled = habilitado,
        singleLine = unaLinea,
        minLines = lineasMinimas,
        textStyle = estiloTexto ?: MaterialTheme.typography.bodyLarge,
        keyboardOptions = opcionesTeclado,
        keyboardActions = accionesTeclado,
        modifier = modifier.fillMaxWidth()
    )
}


@Composable
fun MensajeError(texto: String?, modifier: Modifier = Modifier) {
    var ultimo by remember { mutableStateOf(texto ?: "") }
    LaunchedEffect(texto) { if (texto != null) ultimo = texto }

    AnimatedVisibility(
        visible = texto != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            Icon(
                Icons.Filled.Error,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(Dimens.tamanoIcono)
            )
            Text(texto ?: ultimo, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
