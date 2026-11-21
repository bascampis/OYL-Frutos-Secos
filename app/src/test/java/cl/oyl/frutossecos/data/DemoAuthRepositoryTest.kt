package cl.oyl.frutossecos.data

import cl.oyl.frutossecos.data.repository.DemoAuthRepository
import cl.oyl.frutossecos.domain.model.UserRole
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DemoAuthRepositoryTest {

    private val repo = DemoAuthRepository()
    private val password = DemoAuthRepository.DEMO_PASSWORD

    @Test fun adminSignsInWithDemoPassword() = runTest {
        val user = repo.signIn(UserRole.CLIENTE, "admin@admin.cl", password)
        assertNotNull(user)
        assertEquals(UserRole.ADMINISTRADOR, user!!.role)
    }

    @Test fun clientSignsInWithDemoPassword() = runTest {
        val user = repo.signIn(UserRole.CLIENTE, " Cliente@Cliente.cl ", password)
        assertEquals("cliente", user!!.alias)
        assertEquals(UserRole.CLIENTE, user.role)
    }

    @Test fun wrongPasswordIsRejected() = runTest {
        assertNull(repo.signIn(UserRole.CLIENTE, "admin@admin.cl", "otra-clave"))
    }

    @Test fun unknownUserIsRejected() = runTest {
        assertNull(repo.signIn(UserRole.CLIENTE, "otro@correo.cl", password))
    }
}
