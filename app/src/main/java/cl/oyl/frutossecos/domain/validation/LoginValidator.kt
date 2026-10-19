package cl.oyl.frutossecos.domain.validation

data class LoginErrors(
    val alias: String? = null,
    val password: String? = null
) {
    val isValid: Boolean get() = alias == null && password == null
}

/** Reglas sin dependencias de Compose ni Android. */
object LoginValidator {
    fun validate(alias: String, password: String): LoginErrors = LoginErrors(
        alias = when {
            alias.isBlank() -> "Ingresa tu alias o correo."
            alias.trim().length < 3 -> "Debe tener al menos 3 caracteres."
            else -> null
        },
        password = when {
            password.isBlank() -> "Ingresa tu contraseña."
            password.length < 8 -> "Debe tener al menos 8 caracteres."
            else -> null
        }
    )
}
