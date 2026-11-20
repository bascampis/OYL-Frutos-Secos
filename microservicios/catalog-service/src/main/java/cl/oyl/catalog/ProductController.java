package cl.oyl.catalog;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ProductController {

    public record StockDelta(int delta) {}

    private final ProductService service;

    public ProductController(ProductService service) { this.service = service; }

    @GetMapping("/health")
    public Map<String, String> health() { return Map.of("service", "catalog-service", "status", "ok"); }

    @GetMapping("/products")
    public List<Product> list() { return service.list(); }

    @GetMapping("/products/{id}")
    public Product get(@PathVariable String id) { return service.get(id); }

    @PostMapping("/products") @ResponseStatus(HttpStatus.CREATED)
    public Product create(@RequestBody ProductService.ProductInput in) { return service.create(in); }

    @PutMapping("/products/{id}")
    public Product update(@PathVariable String id, @RequestBody ProductService.ProductInput in) {
        return service.update(id, in);
    }

    @DeleteMapping("/products/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) { service.delete(id); }

    @PatchMapping("/products/{id}/stock")
    public Product stock(@PathVariable String id, @RequestBody StockDelta body) {
        return service.adjustStock(id, body.delta());
    }
}
