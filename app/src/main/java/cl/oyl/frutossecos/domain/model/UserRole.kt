package cl.oyl.frutossecos.domain.model

enum class UserRole { CLIENTE, ADMINISTRADOR }

data class DemoUser(
    val alias: String,
    val displayName: String,
    val role: UserRole
)
