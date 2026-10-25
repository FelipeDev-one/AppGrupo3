package com.example.appgrupo3.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.appgrupo3.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraConVolver(
    titulo: String,
    onVolver: () -> Unit,
    subtitulo: String? = null,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(titulo, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (subtitulo != null) {
                    Text(subtitulo, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_volver))
            }
        },
        actions = acciones,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}
