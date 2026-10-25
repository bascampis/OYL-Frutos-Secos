package cl.oyl.frutossecos.domain.checkout

import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatClp(amount: Long): String = NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
    maximumFractionDigits = 0
    minimumFractionDigits = 0
}.format(amount)

fun formatOrderDate(value: String, zone: ZoneId = ZoneId.systemDefault()): String = try {
    DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm").withZone(zone).format(Instant.parse(value))
} catch (_: Exception) { "Fecha no disponible" }
