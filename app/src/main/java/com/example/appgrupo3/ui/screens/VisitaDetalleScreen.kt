package com.example.appgrupo3.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appgrupo3.model.Calculos
import com.example.appgrupo3.model.EstadoProtocolo
import com.example.appgrupo3.model.Reglas
import com.example.appgrupo3.model.TipoVisita
import com.example.appgrupo3.ui.components.BarraConVolver
import com.example.appgrupo3.ui.components.BannerConexion
import com.example.appgrupo3.ui.components.FilaDato
import com.example.appgrupo3.ui.components.SeccionTarjeta
import com.example.appgrupo3.ui.components.TarjetaProtocolo
import com.example.appgrupo3.ui.theme.Dimens
import com.example.appgrupo3.utils.Tiempo
import com.example.appgrupo3.viewmodel.InduztekViewModel

@Composable
fun VisitaDetalleScreen(
    numero: String,
    app: InduztekViewModel,
    onVolver: () -> Unit,
    onProtocolo: (String) -> Unit,
    onNuevoEquipo: (String) -> Unit,
    onCerrar: () -> Unit,
    onActa: () -> Unit
) {
    val sistema by app.sistema.collectAsStateWithLifecycle()
    val usuario by app.usuario.collectAsStateWithLifecycle()
    val conectado by app.conectado.collectAsStateWithLifecycle()
    val s = sistema ?: return
    val u = usuario ?: return
    val visita = s.visita(numero) ?: return
    val instalacion = s.instalacion(visita.instalacionId)
    val protocolos = s.protocolosDeVisita(numero)
    val conProtocolo = protocolos.filter { it.estado != EstadoProtocolo.ANULADO }.map { it.equipoId }.toSet()
    val disponibles = s.equipos.filter { it.instalacionId == visita.instalacionId && it.id !in conProtocolo }
    val pendientes = Reglas.pendientesCierre(protocolos)
    val hoy = app.hoy()

    Scaffold(
        topBar = {
            Column {
                BarraConVolver(visita.numero, onVolver, subtitulo = instalacion?.nombre)
                BannerConexion(conectado)
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
                SeccionTarjeta("Datos de la visita", Icons.Filled.Info) {
                    FilaDato("Cliente", s.cliente(visita.clienteRut)?.razonSocial ?: "—")
                    FilaDato("Instalación", "${instalacion?.nombre ?: "—"} (${instalacion?.comuna ?: ""})")
                    FilaDato("Fecha", Tiempo.fechaLegible(visita.fecha))
                    FilaDato("Tipo", visita.tipo.etiqueta)
                    FilaDato("Técnico", s.nombre(visita.tecnicoRut))
                    FilaDato("Ingeniero a cargo", s.nombre(visita.ingenieroRut))
                    FilaDato("Estado", if (visita.cerrada) "Cerrada" else "En curso")
                }

                SeccionTarjeta("Protocolos (${protocolos.size})", Icons.Filled.Description) {
                    if (protocolos.isEmpty()) Text("Agrega los equipos que se probarán en esta visita.")
                    protocolos.forEach { p ->
                        val equipo = s.equipo(p.equipoId)
                        TarjetaProtocolo(
                            protocolo = p,
                            equipo = equipo,
                            subtitulo = "Técnico: ${s.nombre(p.tecnicoRut)}",
                            resultado = equipo?.let { Calculos.global(p, it) },
                            atrasado = Reglas.estaAtrasado(p, hoy),
                            paraCliente = false,
                            onClick = { onProtocolo(p.numero) }
                        )
                    }
                }

                if (!visita.cerrada && Reglas.puedeCrearProtocolo(u)) {
                    SeccionTarjeta("Agregar equipo a la visita", Icons.Filled.ElectricalServices) {
                        Text(
                            "Un protocolo por equipo en cada visita.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        disponibles.forEach { equipo ->
                            OutlinedButton(
                                onClick = { app.agregarProtocolo(numero, equipo.id) { onProtocolo(it.numero) } },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.PostAdd, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                                Text("  ${equipo.tag} · ${equipo.tipo.etiqueta}")
                            }
                        }
                        OutlinedButton(onClick = { onNuevoEquipo(visita.instalacionId) }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.AddCircle, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                            Text("  Registrar equipo nuevo")
                        }
                    }
                }

                if (visita.tipo == TipoVisita.PRUEBAS_Y_MANTENIMIENTO && !visita.cerrada) {
                    Text(
                        "Visita con mantenimiento: describe lo realizado en cada protocolo y registra la medición posterior cuando repitas una prueba.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (visita.cerrada) {
                    val r = visita.recepcion
                    SeccionTarjeta("Recepción del cliente", Icons.Filled.TaskAlt) {
                        if (r != null) {
                            FilaDato("Recibió", r.nombre)
                            FilaDato("RUT", r.rut)
                            FilaDato("Cargo", r.cargo)
                            FilaDato("Fecha y hora", Tiempo.fechaHora(r.fechaHora))
                        }
                    }
                    Button(onClick = onActa, modifier = Modifier.fillMaxWidth().height(Dimens.alturaBoton)) {
                        Icon(Icons.Filled.ReceiptLong, contentDescription = null)
                        Text("  Ver acta de terreno")
                    }
                } else if (Reglas.puedeCerrarVisita(u)) {
                    AnimatedVisibility(visible = pendientes.isNotEmpty() && protocolos.isNotEmpty()) {
                        SeccionTarjeta("No se puede cerrar todavía (RN-13)", Icons.Filled.Warning) {
                            pendientes.forEach {
                                Text("• ${it.numero} (${s.equipo(it.equipoId)?.tag}) está en ${it.estado.etiqueta}")
                            }
                        }
                    }
                    Button(
                        onClick = onCerrar,
                        enabled = pendientes.isEmpty() && protocolos.any { it.estado != EstadoProtocolo.ANULADO },
                        modifier = Modifier.fillMaxWidth().height(Dimens.alturaBoton)
                    ) {
                        Icon(Icons.Filled.LockClock, contentDescription = null)
                        Text("  Cerrar visita y entregar acta")
                    }
                } else {
                    Text(
                        "El cierre de la visita lo realiza el técnico o el ingeniero.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
