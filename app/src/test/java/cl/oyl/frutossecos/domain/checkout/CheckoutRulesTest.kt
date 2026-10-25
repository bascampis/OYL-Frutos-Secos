package cl.oyl.frutossecos.domain.checkout

import org.junit.Assert.*
import org.junit.Test
import java.time.ZoneId

class CheckoutRulesTest {
    private val line = CheckoutItem("OY001", "Almendras", 2, 2990)
    private val available = AvailableItem("OY001", "Almendras", 2990, 10, true)

    @Test fun availableProductsCanBeConfirmed() {
        assertTrue(CheckoutRules.review(listOf(line), listOf(available)).canConfirm)
    }
    @Test fun priceChangeRequiresAnotherReview() {
        val review = CheckoutRules.review(listOf(line), listOf(available.copy(price = 3490)))
        assertTrue(review.pricesChanged)
        assertFalse(review.canConfirm)
    }
    @Test fun insufficientStockIdentifiesProductAndRemainingUnits() {
        val review = CheckoutRules.review(listOf(line), listOf(available.copy(stock = 1)))
        assertFalse(review.canConfirm)
        assertTrue(review.problems.single().contains("Almendras"))
        assertTrue(review.problems.single().contains("quedan 1"))
    }
    @Test fun inactiveAndMissingProductsCannotBeConfirmed() {
        assertFalse(CheckoutRules.review(listOf(line), listOf(available.copy(active = false))).canConfirm)
        assertFalse(CheckoutRules.review(listOf(line), emptyList()).canConfirm)
    }
    @Test fun emptyCartCannotBeConfirmed() {
        assertFalse(CheckoutRules.review(emptyList(), listOf(available)).canConfirm)
    }
    @Test fun invalidPriceOrQuantityCannotBeConfirmed() {
        assertFalse(CheckoutRules.review(listOf(line), listOf(available.copy(price = 0))).canConfirm)
        assertFalse(CheckoutRules.review(listOf(line.copy(quantity = 0)), listOf(available)).canConfirm)
    }
    @Test fun dateComesFromServerTimestampInRequestedTimeZone() {
        assertEquals("24/10/2026 · 18:30", formatOrderDate("2026-10-24T21:30:00Z", ZoneId.of("America/Santiago")))
        assertEquals("24/10/2026 · 21:30", formatOrderDate("2026-10-24T21:30:00Z", ZoneId.of("UTC")))
    }
    @Test fun invalidDateDoesNotInventAValue() {
        assertEquals("Fecha no disponible", formatOrderDate("incorrecta"))
    }
}
