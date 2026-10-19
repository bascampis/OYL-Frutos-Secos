package cl.oyl.frutossecos.domain.validation

data class RegisterErrors(val name: String? = null, val alias: String? = null,
 val email: String? = null, val password: String? = null) {
 val isValid get() = name == null && alias == null && email == null && password == null
}
object RegisterValidator {
 fun validate(name: String, alias: String, email: String, password: String): RegisterErrors = RegisterErrors(
  name = if (name.trim().length < 3) "Nombre: mínimo 3 caracteres." else null,
  alias = if (!alias.trim().matches(Regex("[A-Za-z0-9_]{3,24}"))) "Alias: 3-24 letras, números o _." else null,
  email = if (!email.trim().matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))) "Correo no válido." else null,
  password = if (password.length < 8) "Contraseña: mínimo 8 caracteres." else null
 )
}