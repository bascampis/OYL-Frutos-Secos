package cl.oyl.frutossecos.data.repository

import cl.oyl.frutossecos.domain.model.DemoUser
import cl.oyl.frutossecos.domain.model.UserRole

interface AuthRepository {
    suspend fun signIn(role: UserRole, alias: String, password: String): DemoUser?
}
