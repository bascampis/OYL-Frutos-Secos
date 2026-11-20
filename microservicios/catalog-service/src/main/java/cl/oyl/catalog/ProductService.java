package cl.oyl.catalog;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductService {

    public record ProductInput(String id, String name, String category, String description,
                               String presentation, int price, int stock, boolean active, String imageUrl) {}

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) { this.repository = repository; }

    public List<Product> list() { return repository.findAll(); }

    public Product get(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    @Transactional
    public Product create(ProductInput in) {
        validate(in, true);
        if (repository.existsById(in.id()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con ese código");
        return repository.save(apply(new Product(), in, true));
    }

    @Transactional
    public Product update(String id, ProductInput in) {
        validate(in, false);
        Product p = get(id);
        return repository.save(apply(p, in, false));
    }

    @Transactional
    public void delete(String id) {
        repository.delete(get(id));
    }

    @Transactional
    public Product adjustStock(String id, int delta) {
        Product p = get(id);
        long next = (long) p.getStock() + delta;
        if (next < 0 || next > Integer.MAX_VALUE)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente o inválido");
        p.setStock((int) next);
        return repository.save(p);
    }

    private void validate(ProductInput in, boolean requireId) {
        if (requireId && (in.id() == null || !in.id().matches("[A-Za-z0-9_-]{3,24}")))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido (3 a 24 caracteres)");
        if (in.name() == null || in.name().isBlank() || in.category() == null || in.category().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre y categoría son obligatorios");
        if (in.price() < 0 || in.stock() < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Precio y stock no pueden ser negativos");
        if (in.imageUrl() != null && !in.imageUrl().isBlank() && !in.imageUrl().trim().startsWith("https://"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen debe ser una URL https://");
    }

    private Product apply(Product p, ProductInput in, boolean isNew) {
        if (isNew) { p.setId(in.id()); p.setStock(in.stock()); }
        p.setName(in.name().trim());
        p.setCategory(in.category().trim());
        p.setDescription(in.description());
        p.setPresentation(in.presentation());
        p.setPrice(in.price());
        p.setActive(in.active());
        p.setImageUrl(in.imageUrl() == null || in.imageUrl().isBlank() ? null : in.imageUrl().trim());
        return p;
    }
}