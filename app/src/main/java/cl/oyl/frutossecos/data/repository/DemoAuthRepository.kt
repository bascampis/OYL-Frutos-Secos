package cl.oyl.frutossecos.data.repository

import cl.oyl.frutossecos.domain.model.DemoUser
import cl.oyl.frutossecos.domain.model.UserRole

/** Solo para pruebas académicas. NO hay autenticación de servidor. */
class DemoAuthRepository : AuthRepository {
    @Suppress("UNUSED_PARAMETER")
    override suspend fun signIn(role: UserRole, alias: String, password: String): DemoUser? {
        if (password != DEMO_PASSWORD) return null
        return when {
            alias.trim().equals("admin@admin.cl", ignoreCase = true) ->
                DemoUser(alias = "admin@admin.cl", displayName = "Administrador", role = UserRole.ADMINISTRADOR)
            alias.trim().equals("cliente@cliente.cl", ignoreCase = true) ->
                DemoUser(alias = "cliente", displayName = "Cliente Demo", role = UserRole.CLIENTE)
            else -> null
        }
    }

    companion object {
        const val DEMO_PASSWORD = "DemoOYL2026!" // Ficticia, pública y solo de demostración.
    }
}
