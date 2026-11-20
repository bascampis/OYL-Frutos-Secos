package cl.oyl.users;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class UserService {

    public record RegisterInput(String name, String email, String phone, String address, String password) {}
    public record LoginInput(String email, String password) {}
    public record UpdateInput(String name, String phone, String address) {}

    static final String ADMIN_EMAIL = "admin@admin.cl";
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern PHONE = Pattern.compile("^\\+569\\d{8}$");

    private final UserRepository repository;

    public UserService(UserRepository repository) { this.repository = repository; }

    @Transactional
    public AppUser register(RegisterInput in) {
        validate(in);
        if (repository.existsByEmailIgnoreCase(in.email().trim()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        AppUser u = new AppUser();
        u.setName(in.name().trim());
        u.setEmail(in.email().trim().toLowerCase());
        u.setPhone(in.phone().trim());
        u.setAddress(in.address() == null ? "" : in.address().trim());
        u.setRole(roleFor(u.getEmail()));
        u.setSalt(newSalt());
        u.setPasswordHash(hash(u.getSalt(), in.password()));
        return repository.save(u);
    }

    public AppUser login(LoginInput in) {
        if (in.email() == null || in.password() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Correo y contraseña son obligatorios");
        AppUser u = repository.findByEmailIgnoreCase(in.email().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));
        if (!hash(u.getSalt(), in.password()).equals(u.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        return u;
    }

    public List<AppUser> list() { return repository.findAll(); }

    public AppUser get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    @Transactional
    public AppUser update(Long id, UpdateInput in) {
        AppUser u = get(id);
        if (in.name() == null || in.name().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        if (in.phone() == null || !PHONE.matcher(in.phone().trim()).matches())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teléfono inválido (+569 y 8 dígitos)");
        u.setName(in.name().trim());
        u.setPhone(in.phone().trim());
        u.setAddress(in.address() == null ? "" : in.address().trim());
        return repository.save(u);
    }

    @Transactional
    public void delete(Long id) {
        AppUser u = get(id);
        if (ADMIN_EMAIL.equalsIgnoreCase(u.getEmail()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se puede eliminar al administrador");
        repository.delete(u);
    }

    static String roleFor(String email) { return ADMIN_EMAIL.equalsIgnoreCase(email) ? "ADMIN" : "CLIENT"; }

    private void validate(RegisterInput in) {
        if (in.name() == null || in.name().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        if (in.email() == null || !EMAIL.matcher(in.email().trim()).matches())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Correo inválido");
        if (in.phone() == null || !PHONE.matcher(in.phone().trim()).matches())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teléfono inválido (+569 y 8 dígitos)");
        String p = in.password();
        if (p == null || p.length() < 8 || p.chars().noneMatch(Character::isUpperCase) || p.chars().noneMatch(Character::isDigit))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña requiere 8 caracteres, una mayúscula y un número");
    }

    static String newSalt() {
        byte[] b = new byte[16];
        new SecureRandom().nextBytes(b);
        return HexFormat.of().formatHex(b);
    }

    static String hash(String salt, String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest((salt + password).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
