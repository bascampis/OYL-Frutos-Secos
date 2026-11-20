package cl.oyl.orders;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private final OrderRepository repo = mock(OrderRepository.class);
    private final CatalogClient catalog = mock(CatalogClient.class);
    private final OrderService service = new OrderService(repo, catalog);

    private OrderService.OrderRequest request(OrderService.Item... items) {
        return new OrderService.OrderRequest("cliente", "Retiro en tienda", List.of(items));
    }

    @Test
    void createDiscountsStockAndComputesTotal() {
        when(catalog.getProduct("OY001")).thenReturn(new CatalogClient.CatalogProduct("OY001", "Almendras", 2990, 25, true));
        when(repo.save(any(OrderHeader.class))).thenAnswer(i -> i.getArgument(0));
        OrderHeader o = service.create(request(new OrderService.Item("OY001", 2)));
        assertEquals(5980, o.getTotal());
        assertEquals(OrderService.PENDING, o.getStatus());
        verify(catalog).adjustStock("OY001", -2);
    }

    @Test
    void createGroupsRepeatedProducts() {
        when(catalog.getProduct("OY001")).thenReturn(new CatalogClient.CatalogProduct("OY001", "Almendras", 1000, 5, true));
        when(repo.save(any(OrderHeader.class))).thenAnswer(i -> i.getArgument(0));
        OrderHeader o = service.create(request(new OrderService.Item("OY001", 2), new OrderService.Item("OY001", 3)));
        assertEquals(1, o.getLines().size());
        assertEquals(5000, o.getTotal());
    }

    @Test
    void createFailsWhenStockIsNotEnough() {
        when(catalog.getProduct("OY001")).thenReturn(new CatalogClient.CatalogProduct("OY001", "Almendras", 1000, 1, true));
        var e = assertThrows(ResponseStatusException.class, () -> service.create(request(new OrderService.Item("OY001", 2))));
        assertEquals(HttpStatus.CONFLICT, e.getStatusCode());
        verify(catalog, never()).adjustStock(anyString(), anyInt());
        verify(repo, never()).save(any());
    }

    @Test
    void createRejectsInactiveProduct() {
        when(catalog.getProduct("OY001")).thenReturn(new CatalogClient.CatalogProduct("OY001", "Almendras", 1000, 10, false));
        assertThrows(ResponseStatusException.class, () -> service.create(request(new OrderService.Item("OY001", 1))));
    }

    @Test
    void createRestoresStockWhenSecondProductFails() {
        when(catalog.getProduct("OY001")).thenReturn(new CatalogClient.CatalogProduct("OY001", "Almendras", 1000, 10, true));
        when(catalog.getProduct("OY002")).thenReturn(new CatalogClient.CatalogProduct("OY002", "Nueces", 1000, 0, true));
        assertThrows(ResponseStatusException.class,
                () -> service.create(request(new OrderService.Item("OY001", 2), new OrderService.Item("OY002", 1))));
        verify(catalog).adjustStock("OY001", -2);
        verify(catalog).adjustStock("OY001", 2);
    }

    @Test
    void createRejectsEmptyOrder() {
        assertThrows(ResponseStatusException.class,
                () -> service.create(new OrderService.OrderRequest("cliente", "Retiro", List.of())));
    }

    @Test
    void createRejectsInvalidQuantity() {
        assertThrows(ResponseStatusException.class, () -> service.create(request(new OrderService.Item("OY001", 0))));
    }

    @Test
    void updateStatusAcceptsKnownStatus() {
        OrderHeader o = new OrderHeader("cliente", "Retiro");
        when(repo.findById(o.getId())).thenReturn(Optional.of(o));
        when(repo.save(any(OrderHeader.class))).thenAnswer(i -> i.getArgument(0));
        assertEquals("Confirmado", service.updateStatus(o.getId(), "Confirmado").getStatus());
    }

    @Test
    void updateStatusRejectsUnknownStatus() {
        assertThrows(ResponseStatusException.class, () -> service.updateStatus("x", "Inventado"));
    }

    @Test
    void cancelRestoresStockOfPendingOrder() {
        OrderHeader o = new OrderHeader("cliente", "Retiro");
        o.addLine("OY001", "Almendras", 3, 1000);
        when(repo.findById(o.getId())).thenReturn(Optional.of(o));
        service.cancel(o.getId());
        verify(catalog).adjustStock("OY001", 3);
        verify(repo).delete(o);
    }

    @Test
    void cancelRejectsNonPendingOrder() {
        OrderHeader o = new OrderHeader("cliente", "Retiro");
        o.setStatus("Entregado");
        when(repo.findById(o.getId())).thenReturn(Optional.of(o));
        assertThrows(ResponseStatusException.class, () -> service.cancel(o.getId()));
        verify(repo, never()).delete(any());
    }

    @Test
    void byClientDelegatesToRepository() {
        when(repo.findByClientAliasOrderByCreatedAtDesc("cliente")).thenReturn(List.of());
        assertTrue(service.byClient("cliente").isEmpty());
    }

    private static String anyString() { return org.mockito.ArgumentMatchers.anyString(); }
    private static int anyInt() { return org.mockito.ArgumentMatchers.anyInt(); }
}
