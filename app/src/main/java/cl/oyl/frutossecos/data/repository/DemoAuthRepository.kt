package cl.oyl.frutossecos.data.repository

import cl.oyl.frutossecos.domain.model.DemoUser
import cl.oyl.frutossecos.domain.model.UserRole

/** Solo para pruebas académicas. NO hay autenticación de servidor. */
class DemoAuthRepository : AuthRepository {
    override suspend fun signIn(role: UserRole, alias: String, password: String): DemoUser? {
        if (password != DEMO_PASSWORD) return null
        return when {
            role == UserRole.ADMINISTRADOR && alias.trim().equals("admin@admin.cl", ignoreCase = true) ->
                DemoUser(alias = "admin@admin.cl", displayName = "Administrador", role = role)
            role == UserRole.CLIENTE && alias.trim().equals("cliente", ignoreCase = true) ->
                DemoUser(alias = "cliente", displayName = "Cliente Demo", role = role)
            else -> null
        }
    }

    companion object {
        const val DEMO_PASSWORD = "DemoOYL2026!" // Ficticia, pública y solo de demostración.
    }
}
