package com.tecsup.campusgps.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.tecsup.campusgps.model.UbicacionUi
import com.tecsup.campusgps.util.formatearDistancia
import com.tecsup.campusgps.viewmodel.MapaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapa(vm: MapaViewModel) {
    val estado by vm.estado.collectAsState()
    val lugares by vm.lugares.collectAsState()
    val seleccionado by vm.seleccionado.collectAsState()
    val masCercano by vm.masCercano.collectAsState()
    val categoriaSeleccionada by vm.categoriaSeleccionada.collectAsState()
    val radioSeleccionado by vm.radioSeleccionado.collectAsState()
    val lugaresEnRadio by vm.lugaresEnRadio.collectAsState()
    val modoOscuro = isSystemInDarkTheme()

    val contexto = LocalContext.current
    val lanzador = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        if (resultado.values.any { it }) vm.buscar() else vm.permisoDenegado()
    }

    fun tienePermisoUbicacion(): Boolean = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).any { ContextCompat.checkSelfPermission(contexto, it) == PackageManager.PERMISSION_GRANTED }

    fun alPulsar() {
        if (tienePermisoUbicacion()) {
            vm.buscar()
        } else {
            lanzador.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CampusGPS") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (estado !is UbicacionUi.Error) {

                MapaUbicacion(
                    estado = estado,
                    lugares = lugares,
                    seleccionado = seleccionado,
                    masCercano = masCercano?.id,
                    radio = radioSeleccionado,
                    mostrarMiUbicacion = tienePermisoUbicacion(),
                    modoOscuro = modoOscuro,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { alPulsar() },
                    enabled = estado !is UbicacionUi.Buscando
                ) {
                    Text("Obtener mi ubicación")
                }

                if (estado !is UbicacionUi.Error) {
                    OutlinedButton(
                        onClick = { masCercano?.let { vm.seleccionar(it.id) } },
                        enabled = masCercano != null
                    ) {
                        Text("Ir al más cercano")
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        vm.categorias.forEach { cat ->
                            FilterChip(
                                selected = categoriaSeleccionada == cat,
                                onClick = { vm.seleccionarCategoria(cat) },
                                label = { Text(cat) }
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        vm.radiosDisponibles.forEach { radio ->
                            FilterChip(
                                selected = radioSeleccionado == radio,
                                onClick = { vm.seleccionarRadio(radio) },
                                label = { Text("$radio m") }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                when (val e = estado) {
                    UbicacionUi.Inicial -> Text("Pulsa el botón para empezar")
                    UbicacionUi.Buscando -> CircularProgressIndicator()
                    is UbicacionUi.Lista -> Text(
                        text = "Latitud: ${e.posicion.latitud}  ·  Longitud: ${e.posicion.longitud}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    is UbicacionUi.Error -> MensajeError(error = e, onReintentar = { alPulsar() })
                }

                if (estado !is UbicacionUi.Error) masCercano?.let { lugar ->
                    val metros = lugares.firstOrNull { it.lugar.id == lugar.id }?.metros
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Más cercano: ") }
                            append(lugar.nombre)
                            metros?.let { append(" (${formatearDistancia(it)})") }
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (estado !is UbicacionUi.Error) {
                ListaLugares(
                    lugares = lugares,
                    seleccionado = seleccionado,
                    masCercano = masCercano?.id,
                    lugaresEnRadio = lugaresEnRadio,
                    onClick = { vm.seleccionar(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            }
        }
    }
}