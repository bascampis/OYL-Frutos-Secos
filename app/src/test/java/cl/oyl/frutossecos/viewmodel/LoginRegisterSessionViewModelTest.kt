package cl.oyl.frutossecos.viewmodel

import android.app.Application
import android.content.SharedPreferences
import cl.oyl.frutossecos.MainDispatcherRule
import cl.oyl.frutossecos.OylApplication
import cl.oyl.frutossecos.data.repository.LocalAuthRepository
import cl.oyl.frutossecos.domain.model.DemoUser
import cl.oyl.frutossecos.domain.model.UserRole
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginRegisterSessionViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    private val auth = mockk<LocalAuthRepository>()
    private val app = mockk<OylApplication>(relaxed = true).also { every { it.authRepository } returns auth }
    private val user = DemoUser("cliente", "Cata", UserRole.CLIENTE)

    // ---------- Login ----------
    @Test fun login_fieldEditingAndRoleReset() {
        val vm = LoginViewModel(app)
        vm.updateAlias("a@b.cl"); vm.updatePassword("x")
        assertEquals("a@b.cl", vm.uiState.value.alias)
        vm.selectRole(UserRole.ADMINISTRADOR)
        assertEquals("", vm.uiState.value.alias)
        assertEquals(UserRole.ADMINISTRADOR, vm.uiState.value.role)
    }

    @Test fun login_demoCredentialsDependOnRole() {
        val vm = LoginViewModel(app)
        vm.useDemoCredentials()
        assertEquals("cliente@cliente.cl", vm.uiState.value.alias)
        vm.selectRole(UserRole.ADMINISTRADOR)
        vm.useDemoCredentials()
        assertEquals("admin@admin.cl", vm.uiState.value.alias)
    }

    @Test fun login_invalidFormShowsErrorsAndDoesNotCallRepository() {
        val vm = LoginViewModel(app)
        var called = false
        vm.signIn { called = true }
        assertFalse(called)
        assertNotNull(vm.uiState.value.aliasError)
    }

    @Test fun login_success() {
        coEvery { auth.signIn(any(), any(), any()) } returns user
        val vm = LoginViewModel(app)
        vm.useDemoCredentials()
        var result: DemoUser? = null
        vm.signIn { result = it }
        assertEquals(user, result)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test fun login_wrongCredentials() {
        coEvery { auth.signIn(any(), any(), any()) } returns null
        val vm = LoginViewModel(app)
        vm.useDemoCredentials()
        vm.signIn { }
        assertEquals("Correo o contraseña incorrectos.", vm.uiState.value.generalError)
    }

    @Test fun login_repositoryFailure() {
        coEvery { auth.signIn(any(), any(), any()) } throws RuntimeException("db")
        val vm = LoginViewModel(app)
        vm.useDemoCredentials()
        vm.signIn { }
        assertEquals("Error al consultar datos locales.", vm.uiState.value.generalError)
    }

    // ---------- Register ----------
    private fun filled(vm: RegisterViewModel, password: String = "DemoOYL2026!", confirm: String = password) {
        vm.edit("name", "Catalina Aguirre")
        vm.edit("alias", "cata")
        vm.edit("email", " Cata@Correo.cl ")
        vm.edit("phone", "+56912345678")
        vm.edit("address", "Av. Providencia 1234, Santiago")
        vm.edit("password", password)
        vm.edit("confirm", confirm)
    }

    @Test fun register_editStoresEveryField() {
        val vm = RegisterViewModel(app)
        filled(vm)
        val s = vm.state.value
        assertEquals("Catalina Aguirre", s.name)
        assertEquals("cata", s.alias)
        assertEquals("+56912345678", s.phone)
        assertEquals("DemoOYL2026!", s.password)
        assertEquals("DemoOYL2026!", s.confirm)
    }

    @Test fun register_invalidFormShowsGenericError() {
        val vm = RegisterViewModel(app)
        vm.submit { }
        assertEquals("Revisa los campos marcados en rojo.", vm.state.value.error)
    }

    @Test fun register_success() {
        coEvery { auth.register(any(), any(), any(), any(), any(), any()) } returns user
        val vm = RegisterViewModel(app)
        filled(vm)
        var result: DemoUser? = null
        vm.submit { result = it }
        assertEquals(user, result)
        assertFalse(vm.state.value.busy)
    }

    @Test fun register_repositoryRejection() {
        coEvery { auth.register(any(), any(), any(), any(), any(), any()) } throws IllegalArgumentException("Ese correo no está disponible.")
        val vm = RegisterViewModel(app)
        filled(vm)
        vm.submit { }
        assertEquals("Ese correo no está disponible.", vm.state.value.error)
    }

    // ---------- Session ----------
    private fun sessionApp(alias: String?, role: String = "CLIENTE"): Triple<Application, SharedPreferences, SharedPreferences.Editor> {
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { editor.putString(any(), any()) } returns editor
        every { editor.clear() } returns editor
        val prefs = mockk<SharedPreferences>(relaxed = true)
        every { prefs.edit() } returns editor
        every { prefs.getString("alias", null) } returns alias
        every { prefs.getString("name", any()) } returns "Cata"
        every { prefs.getString("role", "") } returns role
        val a = mockk<OylApplication>(relaxed = true)
        every { a.getSharedPreferences("oyl_session", 0) } returns prefs
        every { a.getSystemService(android.app.NotificationManager::class.java) } returns mockk(relaxed = true)
        return Triple(a, prefs, editor)
    }

    @Test fun session_startsEmptyWithoutSavedAlias() {
        val (a, _, _) = sessionApp(null)
        assertNull(SessionViewModel(a).user.value)
    }

    @Test fun session_restoresSavedUserAndRole() {
        val (a, _, _) = sessionApp("admin@admin.cl", "ADMINISTRADOR")
        val u = SessionViewModel(a).user.value
        assertEquals(UserRole.ADMINISTRADOR, u?.role)
        assertEquals("Cata", u?.displayName)
        val (b, _, _) = sessionApp("cliente", "otra")
        assertEquals(UserRole.CLIENTE, SessionViewModel(b).user.value?.role)
    }

    @Test fun session_startAndEnd() {
        val (a, _, editor) = sessionApp(null)
        val vm = SessionViewModel(a)
        vm.startSession(user)
        assertEquals(user, vm.user.value)
        verify { editor.putString("alias", "cliente") }
        vm.endSession()
        assertNull(vm.user.value)
        verify { editor.clear() }
        assertTrue(true)
    }
}
