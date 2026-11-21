package cl.oyl.frutossecos.data

import cl.oyl.frutossecos.data.local.ClientDao
import cl.oyl.frutossecos.data.remote.ApiException
import cl.oyl.frutossecos.data.remote.RemoteRepository
import cl.oyl.frutossecos.data.remote.RemoteUser
import cl.oyl.frutossecos.data.repository.DemoAuthRepository
import cl.oyl.frutossecos.data.repository.LocalAuthRepository
import cl.oyl.frutossecos.domain.model.UserRole
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocalAuthRepositoryTest {

    private val dao = mockk<ClientDao>()
    private val remote = mockk<RemoteRepository>()
    private val repo = LocalAuthRepository(dao, remote)
    private val demoPassword = DemoAuthRepository.DEMO_PASSWORD

    private fun remoteUser(email: String, role: String, name: String = "Nombre") =
        RemoteUser(1, name, email, "+56912345678", "Calle 1", role)

    @Test fun remoteAdminBecomesAdministrator() = runTest {
        coEvery { remote.login("admin@admin.cl", demoPassword) } returns
            Result.success(remoteUser("admin@admin.cl", "ADMIN", "Administrador OYL"))
        val user = repo.signIn(UserRole.CLIENTE, "admin@admin.cl", demoPassword)
        assertEquals(UserRole.ADMINISTRADOR, user!!.role)
        assertEquals("admin@admin.cl", user.alias)
    }

    @Test fun remoteDemoClientKeepsTheClienteAlias() = runTest {
        coEvery { remote.login("cliente@cliente.cl", demoPassword) } returns
            Result.success(remoteUser("cliente@cliente.cl", "CLIENT", "Cliente Demo"))
        val user = repo.signIn(UserRole.CLIENTE, "cliente@cliente.cl", demoPassword)
        assertEquals("cliente", user!!.alias)
    }

    @Test fun remoteClientIsSavedLocallyTheFirstTime() = runTest {
        coEvery { remote.login("ana@correo.cl", "Clave1234") } returns
            Result.success(remoteUser("ana@correo.cl", "CLIENT", "Ana"))
        coEvery { dao.byAlias("ana@correo.cl") } returns null
        coEvery { dao.add(any()) } just Runs
        val user = repo.signIn(UserRole.CLIENTE, "Ana@Correo.cl", "Clave1234")
        assertEquals("ana@correo.cl", user!!.alias)
        coVerify(exactly = 1) { dao.add(any()) }
    }

    @Test fun serverDownFallsBackToDemoAccounts() = runTest {
        coEvery { remote.login(any(), any()) } returns Result.failure(Exception("sin conexión"))
        val user = repo.signIn(UserRole.CLIENTE, "admin@admin.cl", demoPassword)
        assertEquals(UserRole.ADMINISTRADOR, user!!.role)
    }

    @Test fun unknownUserWithServerDownIsRejected() = runTest {
        coEvery { remote.login(any(), any()) } returns Result.failure(ApiException(401, "Credenciales inválidas"))
        coEvery { dao.byAlias(any()) } returns null
        coEvery { dao.byEmail(any()) } returns null
        assertNull(repo.signIn(UserRole.CLIENTE, "nadie@correo.cl", "Clave1234"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun registerRejectsReservedEmails() = runTest {
        repo.register("Ana", "admin@admin.cl", "admin@admin.cl", "+56912345678", "", "Clave1234")
        Unit
    }

    @Test(expected = IllegalArgumentException::class)
    fun registerRejectsAlreadyRegisteredEmails() = runTest {
        coEvery { dao.byAlias("ana@correo.cl") } returns mockk(relaxed = true)
        coEvery { dao.byEmail("ana@correo.cl") } returns null
        repo.register("Ana", "ana@correo.cl", "ana@correo.cl", "+56912345678", "", "Clave1234")
        Unit
    }
}
