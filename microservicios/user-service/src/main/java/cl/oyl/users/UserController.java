package cl.oyl.users;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService service;

    public UserController(UserService service) { this.service = service; }

    @GetMapping("/health")
    public Map<String, String> health() { return Map.of("service", "user-service", "status", "ok"); }

    @PostMapping("/users/register") @ResponseStatus(HttpStatus.CREATED)
    public AppUser register(@RequestBody UserService.RegisterInput in) { return service.register(in); }

    @PostMapping("/users/login")
    public AppUser login(@RequestBody UserService.LoginInput in) { return service.login(in); }

    @GetMapping("/users")
    public List<AppUser> list() { return service.list(); }

    @GetMapping("/users/{id}")
    public AppUser get(@PathVariable Long id) { return service.get(id); }

    @PutMapping("/users/{id}")
    public AppUser update(@PathVariable Long id, @RequestBody UserService.UpdateInput in) {
        return service.update(id, in);
    }

    @DeleteMapping("/users/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
