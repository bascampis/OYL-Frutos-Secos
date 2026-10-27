package cl.oyl.frutossecos.domain.validation

data class RegisterErrors(
 val name: String? = null,
 val email: String? = null,
 val phone: String? = null,
 val address: String? = null,
 val password: String? = null,
 val confirm: String? = null
) {
 val isValid get() = name == null && email == null && phone == null &&
         address == null && password == null && confirm == null
}

object RegisterValidator {
 const val REQUIRED = "Completar campo obligatorio."
 private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
 private val phoneRegex = Regex("^\\+569\\d{8}$")

 fun validate(
  name: String, email: String, phone: String,
  address: String, password: String, confirm: String
 ): RegisterErrors {
  val n = name.trim()
  val e = email.trim()
  val p = phone.trim()
  val a = address.trim()
  return RegisterErrors(
   name = when {
    n.isEmpty() -> REQUIRED
    n.length < 3 -> "Nombre: mínimo 3 caracteres."
    else -> null
   },
   email = when {
    e.isEmpty() -> REQUIRED
    !emailRegex.matches(e) -> "Correo no válido. Ej: nombre@gmail.com"
    else -> null
   },
   phone = when {
    p.isEmpty() -> REQUIRED
    !phoneRegex.matches(p.replace(" ", "")) -> "Debe comenzar con +569 y tener 8 dígitos después."
    else -> null
   },
   address = if (a.isNotEmpty() && a.length < 3) "Dirección: mínimo 3 caracteres." else null,
   password = when {
    password.isEmpty() -> REQUIRED
    password.length < 8 -> "Mínimo 8 caracteres."
    password.none { it.isUpperCase() } -> "Debe incluir una letra mayúscula."
    password.none { it.isDigit() } -> "Debe incluir un número."
    else -> null
   },
   confirm = when {
    confirm.isEmpty() -> REQUIRED
    confirm != password -> "Las contraseñas no coinciden."
    else -> null
   }
  )
 }
}