package cl.oyl.frutossecos.domain.validation

import org.junit.Assert.*
import org.junit.Test

class RegisterValidatorTest {
 private fun v(
  name: String = "Ana Pérez", email: String = "ana@gmail.com", phone: String = "+56912345678",
  address: String = "Santiago", password: String = "Clave1234", confirm: String = password
 ) = RegisterValidator.validate(name, email, phone, address, password, confirm)

 @Test fun validDataAccepted() { assertTrue(v().isValid) }
 @Test fun emptyFieldsRejected() { assertFalse(RegisterValidator.validate("", "", "", "", "", "").isValid) }
 @Test fun blankNameIsRequired() { assertEquals(RegisterValidator.REQUIRED, v(name = "").name) }
 @Test fun malformedEmailRejected() { assertNotNull(v(email = "incorrecto").email) }
 @Test fun phoneMustStartWith569() { assertNotNull(v(phone = "+56812345678").phone) }
 @Test fun phoneWithSpacesAccepted() { assertNull(v(phone = "+56 9 1234 5678").phone) }
 @Test fun shortAddressRejected() { assertNotNull(v(address = "ab").address) }
 @Test fun emptyAddressAccepted() { assertNull(v(address = "").address) }
 @Test fun passwordNeedsUppercaseAndNumber() {
  assertNotNull(v(password = "clave1234").password)
  assertNotNull(v(password = "ClaveClave").password)
 }
 @Test fun confirmMustMatch() { assertNotNull(v(confirm = "Otra1234").confirm) }
}