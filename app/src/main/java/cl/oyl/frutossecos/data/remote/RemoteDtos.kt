package cl.oyl.frutossecos.data.remote

/** Usuario devuelto por user-service (nunca incluye la contraseña). */
data class RemoteUser(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String?,
    val address: String?,
    val role: String
)

data class RegisterBody(
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val password: String
)

data class LoginBody(val email: String, val password: String)

data class UpdateUserBody(val name: String, val phone: String, val address: String)

/** Respuesta de la API externa de tipo de cambio (open.er-api.com). */
data class ExchangeRateResponse(
    val result: String?,
    val base_code: String?,
    val time_last_update_utc: String?,
    val rates: Map<String, Double>?
)
