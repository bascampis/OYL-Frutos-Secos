package cl.oyl.users;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private final UserRepository repo = mock(UserRepository.class);
    private final UserService service = new UserService(repo);

    private UserService.RegisterInput valid(String email, String password) {
        return new UserService.RegisterInput("Ana Pérez", email, "+56912345678", "Calle 1", password);
    }

    private AppUser stored(String email, String password) {
        AppUser u = new AppUser();
        u.setEmail(email); u.setSalt("s");
        u.setPasswordHash(UserService.hash("s", password));
        return u;
    }

    @Test
    void registerHashesPasswordAndAssignsClientRole() {
        when(repo.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(repo.save(any(AppUser.class))).thenAnswer(i -> i.getArgument(0));
        AppUser u = service.register(valid("Ana@Correo.cl", "Clave1234"));
        assertEquals("CLIENT", u.getRole());
        assertEquals("ana@correo.cl", u.getEmail());
        assertNotEquals("Clave1234", u.getPasswordHash());
        assertEquals(UserService.hash(u.getSalt(), "Clave1234"), u.getPasswordHash());
    }

    @Test
    void registerAdminEmailGetsAdminRole() {
        when(repo.save(any(AppUser.class))).thenAnswer(i -> i.getArgument(0));
        assertEquals("ADMIN", service.register(valid("admin@admin.cl", "Clave1234")).getRole());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(repo.existsByEmailIgnoreCase(any())).thenReturn(true);
        var e = assertThrows(ResponseStatusException.class, () -> service.register(valid("a@b.cl", "Clave1234")));
        assertEquals(409, e.getStatusCode().value());
    }

    @Test
    void registerRejectsInvalidEmail() {
        assertThrows(ResponseStatusException.class, () -> service.register(valid("no-es-correo", "Clave1234")));
    }

    @Test
    void registerRejectsInvalidPhone() {
        var in = new UserService.RegisterInput("Ana", "a@b.cl", "12345", "", "Clave1234");
        assertThrows(ResponseStatusException.class, () -> service.register(in));
    }

    @Test
    void registerRejectsWeakPasswords() {
        assertThrows(ResponseStatusException.class, () -> service.register(valid("a@b.cl", "corta1A")));
        assertThrows(ResponseStatusException.class, () -> service.register(valid("a@b.cl", "sinmayuscula1")));
        assertThrows(ResponseStatusException.class, () -> service.register(valid("a@b.cl", "SinNumeroAqui")));
    }

    @Test
    void registerRejectsBlankName() {
        var in = new UserService.RegisterInput(" ", "a@b.cl", "+56912345678", "", "Clave1234");
        assertThrows(ResponseStatusException.class, () -> service.register(in));
    }

    @Test
    void loginSucceedsWithCorrectPassword() {
        when(repo.findByEmailIgnoreCase("a@b.cl")).thenReturn(Optional.of(stored("a@b.cl", "Clave1234")));
        assertEquals("a@b.cl", service.login(new UserService.LoginInput("a@b.cl", "Clave1234")).getEmail());
    }

    @Test
    void loginFailsWithWrongPassword() {
        when(repo.findByEmailIgnoreCase("a@b.cl")).thenReturn(Optional.of(stored("a@b.cl", "Clave1234")));
        var e = assertThrows(ResponseStatusException.class,
                () -> service.login(new UserService.LoginInput("a@b.cl", "Otra12345")));
        assertEquals(401, e.getStatusCode().value());
    }

    @Test
    void loginFailsForUnknownUser() {
        when(repo.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.login(new UserService.LoginInput("x@y.cl", "Clave1234")));
    }

    @Test
    void updateChangesProfileFields() {
        AppUser u = stored("a@b.cl", "Clave1234");
        when(repo.findById(1L)).thenReturn(Optional.of(u));
        when(repo.save(any(AppUser.class))).thenAnswer(i -> i.getArgument(0));
        AppUser r = service.update(1L, new UserService.UpdateInput("Nuevo Nombre", "+56987654321", "Otra calle"));
        assertEquals("Nuevo Nombre", r.getName());
        assertEquals("Otra calle", r.getAddress());
    }

    @Test
    void updateRejectsBadPhone() {
        when(repo.findById(1L)).thenReturn(Optional.of(stored("a@b.cl", "Clave1234")));
        assertThrows(ResponseStatusException.class, () -> service.update(1L, new UserService.UpdateInput("N", "123", "")));
    }

    @Test
    void deleteBlocksAdmin() {
        when(repo.findById(1L)).thenReturn(Optional.of(stored("admin@admin.cl", "Clave1234")));
        var e = assertThrows(ResponseStatusException.class, () -> service.delete(1L));
        assertEquals(403, e.getStatusCode().value());
    }

    @Test
    void deleteRemovesClient() {
        AppUser u = stored("a@b.cl", "Clave1234");
        when(repo.findById(2L)).thenReturn(Optional.of(u));
        service.delete(2L);
        verify(repo).delete(u);
    }
}
