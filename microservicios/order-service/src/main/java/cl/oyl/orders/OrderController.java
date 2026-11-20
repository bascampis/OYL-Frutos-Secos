package cl.oyl.orders;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class OrderController {

    public record StatusBody(String status) {}

    private final OrderService service;

    public OrderController(OrderService service) { this.service = service; }

    @GetMapping("/health")
    public Map<String, String> health() { return Map.of("service", "order-service", "status", "ok"); }

    @PostMapping("/orders") @ResponseStatus(HttpStatus.CREATED)
    public OrderHeader create(@RequestBody OrderService.OrderRequest req) { return service.create(req); }

    /** Sin parámetro: todos los pedidos (administrador). Con clientAlias: solo los del cliente. */
    @GetMapping("/orders")
    public List<OrderHeader> list(@RequestParam(required = false) String clientAlias) {
        return clientAlias == null ? service.all() : service.byClient(clientAlias);
    }

    @GetMapping("/orders/{id}")
    public OrderHeader get(@PathVariable String id) { return service.get(id); }

    @PatchMapping("/orders/{id}/status")
    public OrderHeader status(@PathVariable String id, @RequestBody StatusBody body) {
        return service.updateStatus(id, body.status());
    }

    @DeleteMapping("/orders/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable String id) { service.cancel(id); }
}
