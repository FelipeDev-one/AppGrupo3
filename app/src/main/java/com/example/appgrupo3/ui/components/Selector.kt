package com.example.appgrupo3.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class Opcion<T>(val valor: T, val texto: String, val detalle: String? = null, val habilitada: Boolean = true)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> Selector(
    etiqueta: String,
    icono: ImageVector,
    opciones: List<Opcion<T>>,
    seleccionado: T?,
    onSeleccion: (T) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    ayuda: String? = null,
    habilitado: Boolean = true,
    textoVacio: String = "Seleccionar"
) {
    var abierto by remember { mutableStateOf(false) }
    val actual = opciones.firstOrNull { it.valor == seleccionado }

    ExposedDropdownMenuBox(
        expanded = abierto && habilitado,
        onExpandedChange = { if (habilitado) abierto = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = actual?.texto ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = habilitado,
            label = { Text(etiqueta) },
            placeholder = { Text(textoVacio) },
            leadingIcon = { Icon(icono, contentDescription = null) },
            trailingIcon = {
                if (error != null) Icon(Icons.Filled.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                else ExposedDropdownMenuDefaults.TrailingIcon(expanded = abierto)
            },
            isError = error != null,
            supportingText = (error ?: ayuda)?.let { texto ->
                {
                    Text(
                        texto,
                        color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, habilitado)
        )
        ExposedDropdownMenu(expanded = abierto && habilitado, onDismissRequest = { abierto = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(opcion.texto)
                            if (opcion.detalle != null) {
                                Text(
                                    opcion.detalle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (opcion.habilitada) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    },
                    enabled = opcion.habilitada,
                    onClick = {
                        onSeleccion(opcion.valor)
                        abierto = false
                    }
                )
            }
        }
    }
}
