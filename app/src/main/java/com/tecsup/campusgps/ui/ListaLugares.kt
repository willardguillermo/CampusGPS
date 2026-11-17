package com.tecsup.campusgps.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tecsup.campusgps.model.LugarConDistancia
import com.tecsup.campusgps.util.formatearDistancia

private val ColorPuntoLugar = Color(0xFF1E88E5)
private val ColorPuntoCercano = Color(0xFF34A853)
private val ColorTextoRadio = Color(0xFF34A853)

@Composable
fun ListaLugares(
    lugares: List<LugarConDistancia>,
    seleccionado: Int?,
    masCercano: Int?,
    lugaresEnRadio: Set<Int> = emptySet(),
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(lugares, key = { it.lugar.id }) { item ->
            TarjetaLugar(
                item = item,
                seleccionada = item.lugar.id == seleccionado,
                esMasCercano = item.lugar.id == masCercano,
                estaEnRadio = item.lugar.id in lugaresEnRadio,
                onClick = { onClick(item.lugar.id) }
            )
        }
    }
}

@Composable
private fun TarjetaLugar(
    item: LugarConDistancia,
    seleccionada: Boolean,
    esMasCercano: Boolean,
    estaEnRadio: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionada) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (seleccionada) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        if (esMasCercano) ColorPuntoCercano else ColorPuntoLugar,
                        CircleShape
                    )
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.lugar.nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.lugar.categoria,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (estaEnRadio) {
                    Text(
                        text = "Dentro del radio",
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorTextoRadio,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Text(
                text = item.metros?.let(::formatearDistancia) ?: "Sin calcular",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}