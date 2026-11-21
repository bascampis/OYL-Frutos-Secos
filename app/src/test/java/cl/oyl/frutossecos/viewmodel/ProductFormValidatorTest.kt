package cl.oyl.frutossecos.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductFormValidatorTest {

    private fun validForm() = ProductForm(
        id = "OY100",
        name = "Maní",
        category = "Frutos secos",
        description = "Maní tostado",
        presentation = "250 g",
        price = "1990",
        stock = "10",
        imageUrl = ""
    )

    private fun errors(form: ProductForm, ids: Set<String> = emptySet(), editing: Boolean = false) =
        ProductFormValidator.validate(form, ids, editing)

    @Test fun validFormHasNoErrors() {
        assertTrue(errors(validForm()).isEmpty())
    }

    @Test fun blankIdIsRejected() {
        assertTrue(errors(validForm().copy(id = " ")).containsKey("id"))
    }

    @Test fun idWithInvalidCharactersIsRejected() {
        assertTrue(errors(validForm().copy(id = "OY 1!")).containsKey("id"))
    }

    @Test fun duplicatedIdIsRejectedWhenCreating() {
        assertTrue(errors(validForm(), setOf("oy100")).containsKey("id"))
    }

    @Test fun duplicatedIdIsAllowedWhenEditing() {
        assertFalse(errors(validForm(), setOf("OY100"), editing = true).containsKey("id"))
    }

    @Test fun blankTextFieldsAreRejected() {
        val e = errors(validForm().copy(name = "", category = "", description = "", presentation = ""))
        assertTrue(e.keys.containsAll(listOf("name", "category", "description", "presentation")))
    }

    @Test fun priceMustBeAPositiveInteger() {
        assertTrue(errors(validForm().copy(price = "0")).containsKey("price"))
        assertTrue(errors(validForm().copy(price = "-5")).containsKey("price"))
        assertTrue(errors(validForm().copy(price = "abc")).containsKey("price"))
        assertTrue(errors(validForm().copy(price = "")).containsKey("price"))
    }

    @Test fun stockIsOnlyValidatedWhenCreating() {
        assertTrue(errors(validForm().copy(stock = "-1")).containsKey("stock"))
        assertTrue(errors(validForm().copy(stock = "x")).containsKey("stock"))
        assertFalse(errors(validForm().copy(stock = "x"), editing = true).containsKey("stock"))
    }

    @Test fun imageUrlMustBeHttpsWhenPresent() {
        assertTrue(errors(validForm().copy(imageUrl = "http://x.cl/a.png")).containsKey("imageUrl"))
        assertTrue(errors(validForm().copy(imageUrl = "https://sinpunto")).containsKey("imageUrl"))
        assertFalse(errors(validForm().copy(imageUrl = "https://picsum.photos/200")).containsKey("imageUrl"))
    }

    @Test fun errorMessagesAreInSpanish() {
        assertEquals("Ingresa el nombre.", errors(validForm().copy(name = ""))["name"])
    }
}
