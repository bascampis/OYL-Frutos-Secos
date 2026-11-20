package cl.oyl.orders;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class OrderService {

    public static final String PENDING = "Pendiente de Validación";
    public static final List<String> STATUSES = List.of(PENDING, "Confirmado", "Preparado", "Entregado");

    public record Item(String productId, int quantity) {}
    public record OrderRequest(String clientAlias, String fulfillment, List<Item> items) {}

    private final OrderRepository repository;
    private final CatalogClient catalog;

    public OrderService(OrderRepository repository, CatalogClient catalog) {
        this.repository = repository;
        this.catalog = catalog;
    }

    @Transactional
    public OrderHeader create(OrderRequest req) {
        if (req.clientAlias() == null || req.clientAlias().isBlank()
                || req.fulfillment() == null || req.fulfillment().isBlank()
                || req.items() == null || req.items().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pedido incompleto");

        // Agrupa ids repetidos para no eludir la validación de stock
        SortedMap<String, Integer> quantities = new TreeMap<>();
        for (Item item : req.items()) {
            if (item.productId() == null || item.quantity() < 1)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cantidad inválida");
            quantities.merge(item.productId(), item.quantity(), Integer::sum);
        }

        OrderHeader order = new OrderHeader(req.clientAlias().trim(), req.fulfillment().trim());
        Map<String, Integer> reserved = new LinkedHashMap<>();
        try {
            for (var entry : quantities.entrySet()) {
                CatalogClient.CatalogProduct p = catalog.getProduct(entry.getKey());
                if (!p.active() || p.stock() < entry.getValue())
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente: " + p.name());
                catalog.adjustStock(p.id(), -entry.getValue());
                reserved.put(p.id(), entry.getValue());
                order.addLine(p.id(), p.name(), entry.getValue(), p.price());
            }
        } catch (RuntimeException e) {
            // compensación: devolver el stock ya descontado
            reserved.forEach((id, qty) -> catalog.adjustStock(id, qty));
            throw e;
        }
        return repository.save(order);
    }

    public List<OrderHeader> all() { return repository.findAll(); }

    public List<OrderHeader> byClient(String alias) { return repository.findByClientAliasOrderByCreatedAtDesc(alias); }

    public OrderHeader get(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
    }

    @Transactional
    public OrderHeader updateStatus(String id, String status) {
        if (!STATUSES.contains(status))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado no permitido");
        OrderHeader o = get(id);
        o.setStatus(status);
        return repository.save(o);
    }

    /** Cancela un pedido pendiente y repone el stock en el catálogo. */
    @Transactional
    public void cancel(String id) {
        OrderHeader o = get(id);
        if (!PENDING.equals(o.getStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se puede cancelar un pedido pendiente");
        o.getLines().forEach(l -> catalog.adjustStock(l.getProductId(), l.getQuantity()));
        repository.delete(o);
    }
}
