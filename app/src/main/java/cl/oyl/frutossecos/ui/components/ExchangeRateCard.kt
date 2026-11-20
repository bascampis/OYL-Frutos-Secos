package cl.oyl.frutossecos.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.oyl.frutossecos.viewmodel.ExchangeRateViewModel
import cl.oyl.frutossecos.viewmodel.ExchangeUiState
import java.text.NumberFormat
import java.util.Locale

/** Tarjeta "Dólar hoy" alimentada por la API externa. Agrégala en el inicio o en el catálogo. */
@Composable
fun ExchangeRateCard(modifier: Modifier = Modifier, vm: ExchangeRateViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Dólar hoy (insumos importados)", style = MaterialTheme.typography.titleSmall)
            when (val s = state) {
                is ExchangeUiState.Loading -> CircularProgressIndicator(Modifier.padding(top = 8.dp))
                is ExchangeUiState.Success -> {
                    val clp = NumberFormat.getNumberInstance(Locale("es", "CL")).format(s.clpPerUsd)
                    Text("1 USD = $clp CLP", style = MaterialTheme.typography.headlineSmall)
                    Text("Fuente: open.er-api.com", style = MaterialTheme.typography.bodySmall)
                }
                is ExchangeUiState.Error -> Row {
                    Text(s.message, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { vm.load() }) { Text("Reintentar") }
                }
            }
        }
    }
}
