package cl.oyl.frutossecos.domain.validation

import org.junit.Assert.*
import org.junit.Test

class RegisterValidatorTest {
 @Test fun emptyFieldsRejected() {
  assertFalse(RegisterValidator.validate("", "", "", "").isValid)
 }
 @Test fun validDemoDataAccepted() {
  assertTrue(RegisterValidator.validate("Ana Pérez", "ana_2026", "ana@example.com", "test12345").isValid)
 }
 @Test fun shortAliasRejected() {
  assertNotNull(RegisterValidator.validate("Ana Pérez", "xy", "ana@example.com", "test12345").alias)
 }
 @Test fun malformedEmailRejected() {
  assertNotNull(RegisterValidator.validate("Ana Pérez", "ana_2026", "incorrecto", "test12345").email)
 }
}