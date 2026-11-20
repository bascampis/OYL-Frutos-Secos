package cl.oyl.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private final ProductRepository repo = mock(ProductRepository.class);
    private final ProductService service = new ProductService(repo);

    private ProductService.ProductInput input(String id, int price, int stock, String img) {
        return new ProductService.ProductInput(id, "Nueces", "Frutos secos", "desc", "250 g", price, stock, true, img);
    }

    private Product product(String id, int stock) {
        Product p = new Product();
        p.setId(id); p.setName("Nueces"); p.setStock(stock); p.setActive(true);
        return p;
    }

    @Test
    void createSavesValidProduct() {
        when(repo.existsById("OY100")).thenReturn(false);
        when(repo.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        Product p = service.create(input("OY100", 1000, 5, null));
        assertEquals("OY100", p.getId());
        assertEquals(5, p.getStock());
    }

    @Test
    void createRejectsDuplicateId() {
        when(repo.existsById("OY100")).thenReturn(true);
        var e = assertThrows(ResponseStatusException.class, () -> service.create(input("OY100", 1000, 5, null)));
        assertEquals(409, e.getStatusCode().value());
    }

    @Test
    void createRejectsNegativePrice() {
        assertThrows(ResponseStatusException.class, () -> service.create(input("OY100", -1, 5, null)));
    }

    @Test
    void createRejectsInvalidCode() {
        assertThrows(ResponseStatusException.class, () -> service.create(input("a", 1000, 5, null)));
    }

    @Test
    void createRejectsNonHttpsImage() {
        assertThrows(ResponseStatusException.class, () -> service.create(input("OY100", 1000, 5, "http://x.cl/a.png")));
    }

    @Test
    void createAcceptsHttpsImage() {
        when(repo.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        Product p = service.create(input("OY100", 1000, 5, " https://picsum.photos/200 "));
        assertEquals("https://picsum.photos/200", p.getImageUrl());
    }

    @Test
    void getThrows404WhenMissing() {
        when(repo.findById("X")).thenReturn(Optional.empty());
        var e = assertThrows(ResponseStatusException.class, () -> service.get("X"));
        assertEquals(404, e.getStatusCode().value());
    }

    @Test
    void updateKeepsStock() {
        Product existing = product("OY001", 25);
        when(repo.findById("OY001")).thenReturn(Optional.of(existing));
        when(repo.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        Product p = service.update("OY001", input("OY001", 3000, 999, null));
        assertEquals(3000, p.getPrice());
        assertEquals(25, p.getStock());
    }

    @Test
    void adjustStockAddsAndSubtracts() {
        Product existing = product("OY001", 10);
        when(repo.findById("OY001")).thenReturn(Optional.of(existing));
        when(repo.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        assertEquals(14, service.adjustStock("OY001", 4).getStock());
        assertEquals(9, service.adjustStock("OY001", -5).getStock());
    }

    @Test
    void adjustStockRejectsNegativeResult() {
        when(repo.findById("OY001")).thenReturn(Optional.of(product("OY001", 3)));
        var e = assertThrows(ResponseStatusException.class, () -> service.adjustStock("OY001", -4));
        assertEquals(409, e.getStatusCode().value());
    }

    @Test
    void deleteRemovesProduct() {
        Product existing = product("OY001", 1);
        when(repo.findById("OY001")).thenReturn(Optional.of(existing));
        service.delete("OY001");
        verify(repo).delete(existing);
    }
}
