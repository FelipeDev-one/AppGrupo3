package com.example.appgrupo3.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.appgrupo3.ui.theme.Dimens

@Composable
fun FormularioScaffold(titulo: String, subtitulo: String?, onVolver: () -> Unit, contenido: @Composable () -> Unit) {
    Scaffold(topBar = { BarraConVolver(titulo, onVolver, subtitulo = subtitulo) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(Modifier.widthIn(max = Dimens.anchoMaximoFormulario), verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                contenido()
            }
        }
    }
}
