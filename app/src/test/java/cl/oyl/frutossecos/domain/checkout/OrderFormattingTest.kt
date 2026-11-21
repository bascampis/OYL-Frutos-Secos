package cl.oyl.frutossecos.domain.checkout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

class OrderFormattingTest {

    @Test fun clpHasNoDecimalsAndKeepsTheDigits() {
        val digits = formatClp(2990).filter { it.isDigit() }
        assertEquals("2990", digits)
    }

    @Test fun clpFormatsLargeAmounts() {
        assertEquals("1250000", formatClp(1_250_000).filter { it.isDigit() })
    }

    @Test fun orderDateIsFormattedInTheGivenZone() {
        val text = formatOrderDate("2026-11-20T15:30:00Z", ZoneId.of("UTC"))
        assertEquals("20/11/2026 · 15:30", text)
    }

    @Test fun invalidDateShowsFallbackText() {
        assertEquals("Fecha no disponible", formatOrderDate("no-es-fecha"))
    }

    @Test fun emptyDateShowsFallbackText() {
        assertTrue(formatOrderDate("").contains("no disponible"))
    }
}
