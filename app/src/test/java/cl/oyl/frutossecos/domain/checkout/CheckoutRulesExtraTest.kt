package cl.oyl.frutossecos.domain.checkout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckoutRulesExtraTest {

    private val available = AvailableItem("OY001", "Almendras", 2990, 10, true)

    @Test fun zeroQuantityIsInvalid() {
        val review = CheckoutRules.review(listOf(CheckoutItem("OY001", "Almendras", 0, 2990)), listOf(available))
        assertFalse(review.canConfirm)
        assertTrue(review.problems.single().contains("cantidad inválida"))
    }

    @Test fun productWithoutPriceIsRejected() {
        val review = CheckoutRules.review(
            listOf(CheckoutItem("OY001", "Almendras", 1, 2990)),
            listOf(available.copy(price = 0))
        )
        assertTrue(review.problems.single().contains("precio no disponible"))
    }

    @Test fun emptyCartReportsAMessage() {
        val review = CheckoutRules.review(emptyList(), listOf(available))
        assertEquals("Tu carrito está vacío.", review.problems.single())
    }

    @Test fun severalLinesAccumulateProblems() {
        val review = CheckoutRules.review(
            listOf(
                CheckoutItem("OY001", "Almendras", 50, 2990),
                CheckoutItem("OY999", "Fantasma", 1, 1000)
            ),
            listOf(available)
        )
        assertEquals(2, review.problems.size)
    }

    @Test fun matchingPricesDoNotRequireReview() {
        val review = CheckoutRules.review(listOf(CheckoutItem("OY001", "Almendras", 1, 2990)), listOf(available))
        assertFalse(review.pricesChanged)
    }
}
