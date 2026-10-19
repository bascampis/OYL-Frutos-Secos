package cl.oyl.frutossecos.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginValidatorTest {
    @Test fun camposVaciosEntreganErrores() {
        val errors = LoginValidator.validate("", "")
        assertFalse(errors.isValid)
        assertEquals("Ingresa tu alias o correo.", errors.alias)
        assertEquals("Ingresa tu contraseña.", errors.password)
    }

    @Test fun aliasCortoYClaveCortaSonInvalidos() {
        assertFalse(LoginValidator.validate("ab", "1234567").isValid)
    }

    @Test fun formularioCorrectoPasaValidacion() {
        assertTrue(LoginValidator.validate("cliente", "DemoOYL2026!").isValid)
    }
}
