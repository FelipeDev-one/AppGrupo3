package com.example.appgrupo3.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appgrupo3.R
import com.example.appgrupo3.model.Calculos
import com.example.appgrupo3.model.EstadoProtocolo
import com.example.appgrupo3.model.Perfil
import com.example.appgrupo3.model.Reglas
import com.example.appgrupo3.navigation.Ruta
import com.example.appgrupo3.ui.components.CampoTexto
import com.example.appgrupo3.ui.components.EstadoVacio
import com.example.appgrupo3.ui.components.Opcion
import com.example.appgrupo3.ui.components.PanelScaffold
import com.example.appgrupo3.ui.components.Selector
import com.example.appgrupo3.ui.components.TarjetaProtocolo
import com.example.appgrupo3.ui.theme.Dimens
import com.example.appgrupo3.viewmodel.BusquedaViewModel
import com.example.appgrupo3.viewmodel.InduztekViewModel

@Composable
fun ProtocolosScreen(
    navController: NavController,
    app: InduztekViewModel,
    viewModel: BusquedaViewModel = viewModel()
) {
    val sistema by app.sistema.collectAsStateWithLifecycle()
    val usuario by app.usuario.collectAsStateWithLifecycle()
    val filtros by viewModel.filtros.collectAsStateWithLifecycle()
    val s = sistema ?: return
    val u = usuario ?: return
    val esCliente = u.perfil == Perfil.CLIENTE
    val hoy = app.hoy()
    val lista = viewModel.protocolos(s, u, hoy)

    val estados = if (esCliente) listOf(EstadoProtocolo.FORMALIZADO, EstadoProtocolo.REGISTRADO) else EstadoProtocolo.entries

    PanelScaffold(
        titulo = stringResource(R.string.titulo_protocolos),
        rutaActual = Ruta.Protocolos,
        navController = navController,
        viewModel = app,
        acciones = {
            IconButton(onClick = viewModel::limpiar) { Icon(Icons.Filled.FilterAltOff, contentDescription = "Limpiar filtros") }
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(Dimens.espacioMedio, Dimens.espacioMedio, Dimens.espacioMedio, Dimens.espacioBajoLista),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(Modifier.widthIn(max = Dimens.anchoMaximoLista), verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    CampoTexto(
                        valor = filtros.texto,
                        onCambio = viewModel::onTexto,
                        etiqueta = "Buscar por TAG, serie o instalación",
                        icono = Icons.Filled.Search,
                        error = null
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                        item { FilterChip(selected = filtros.estado == null, onClick = { viewModel.onEstado(null) }, label = { Text("Todos") }) }
                        items(estados) { e ->
                            val texto = if (esCliente && e == EstadoProtocolo.REGISTRADO) Reglas.PENDIENTE else e.etiqueta
                            FilterChip(selected = filtros.estado == e, onClick = { viewModel.onEstado(e) }, label = { Text(texto) })
                        }
                        if (!esCliente) {
                            item {
                                FilterChip(selected = filtros.soloAtrasados, onClick = { viewModel.onSoloAtrasados(!filtros.soloAtrasados) }, label = { Text("Atrasados") })
                            }
                        }
                    }
                    if (!esCliente) {
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                            Selector(
                                etiqueta = "Cliente",
                                icono = Icons.Filled.Business,
                                opciones = listOf(Opcion<String?>(null, "Todos")) + s.clientes.map { Opcion<String?>(it.rut, it.razonSocial) },
                                seleccionado = filtros.clienteRut,
                                onSeleccion = viewModel::onCliente,
                                textoVacio = "Todos",
                                modifier = Modifier.weight(1f)
                            )
                            Selector(
                                etiqueta = "Técnico",
                                icono = Icons.Filled.Engineering,
                                opciones = listOf(Opcion<String?>(null, "Todos")) + s.tecnicos.map { Opcion<String?>(it.rut, it.nombre) },
                                seleccionado = filtros.tecnicoRut,
                                onSeleccion = viewModel::onTecnico,
                                textoVacio = "Todos",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Text("${lista.size} protocolo(s)")
                }
            }
            if (lista.isEmpty()) item { EstadoVacio(Icons.Filled.Description, "No hay protocolos con estos filtros") }
            items(lista, key = { it.numero }) { p ->
                val equipo = s.equipo(p.equipoId)
                val instalacion = equipo?.let { s.instalacion(it.instalacionId) }
                TarjetaProtocolo(
                    protocolo = p,
                    equipo = equipo,
                    subtitulo = if (esCliente) instalacion?.nombre.orEmpty()
                    else "${s.cliente(instalacion?.clienteRut)?.razonSocial.orEmpty()} · ${s.nombre(p.tecnicoRut)}",
                    resultado = equipo?.let { Calculos.global(p, it) },
                    atrasado = Reglas.estaAtrasado(p, hoy),
                    paraCliente = esCliente,
                    onClick = { navController.navigate(Ruta.Protocolo.crear(p.numero)) },
                    modifier = Modifier
                        .widthIn(max = Dimens.anchoMaximoLista)
                        .fillMaxWidth()
                        .animateItem()
                )
            }
        }
    }
}
