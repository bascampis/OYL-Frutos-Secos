package cl.oyl.frutossecos.domain.validation

data class RegisterErrors(
 val name: String? = null,
 val email: String? = null,
 val phone: String? = null,
 val password: String? = null
) {
 val isValid get() = name == null && email == null && phone == null && password == null
}

object RegisterValidator {
 fun validate(name: String, email: String, phone: String, password: String): RegisterErrors = RegisterErrors(
  name = if (name.trim().length < 3) "Nombre: mínimo 3 caracteres." else null,
  email = if (!email.trim().matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))) "Correo no válido." else null,
  phone = if (phone.count { it.isDigit() } < 8) "Teléfono: mínimo 8 dígitos." else null,
  password = if (password.length < 8) "Contraseña: mínimo 8 caracteres." else null
 )
}