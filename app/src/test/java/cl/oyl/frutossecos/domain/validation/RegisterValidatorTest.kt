package cl.oyl.frutossecos.domain.validation

import org.junit.Assert.*
import org.junit.Test

class RegisterValidatorTest {
 @Test fun emptyFieldsRejected() {
  assertFalse(RegisterValidator.validate("", "", "", "").isValid)
 }
 @Test fun validDemoDataAccepted() {
  assertTrue(RegisterValidator.validate("Ana Pérez", "ana@example.com", "+56 9 1234 5678", "test12345").isValid)
 }
 @Test fun shortNameRejected() {
  assertNotNull(RegisterValidator.validate("An", "ana@example.com", "+56 9 1234 5678", "test12345").name)
 }
 @Test fun malformedEmailRejected() {
  assertNotNull(RegisterValidator.validate("Ana Pérez", "incorrecto", "+56 9 1234 5678", "test12345").email)
 }
 @Test fun invalidPhoneRejected() {
  assertNotNull(RegisterValidator.validate("Ana Pérez", "ana@example.com", "abc", "test12345").phone)
 }
}