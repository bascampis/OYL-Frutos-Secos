package cl.oyl.backend.web;

import cl.oyl.backend.model.*;
import cl.oyl.backend.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;
import java.util.*;

@Service
public class OrderService {
 private final ProductRepository products;
 private final OrderRepository orders;
 public OrderService(ProductRepository products,OrderRepository orders) {this.products=products;this.orders=orders;}
 @Transactional
 public OrderHeader create(OrderRequest req) {
  // Agrupar ids repetidos para evitar elusión de la validación de stock.
  SortedMap<String,Integer> quantities=new TreeMap<>();
  for (OrderRequest.Item item:req.items()) {
   quantities.merge(item.productId(),item.quantity(),(a,b)->Math.addExact(a,b));
  }
  OrderHeader order=new OrderHeader(req.clientAlias().trim(),req.fulfillment().trim());
  // Bloqueo pesimista con orden estable reduce condiciones de carrera entre compras.
  for (var entry:quantities.entrySet()) {
   Product product=products.lockById(entry.getKey())
    .orElseThrow(()->new ResponseStatusException(NOT_FOUND,"Producto no existe: "+entry.getKey()));
   int qty=entry.getValue();
   if (!product.isActive() || product.getStock()<qty)
    throw new ResponseStatusException(CONFLICT,"Stock insuficiente: "+product.getName());
   if (product.getPrice()<=0) throw new ResponseStatusException(CONFLICT,"Precio no válido");
   product.setStock(product.getStock()-qty);
   order.addLine(product.getId(),product.getName(),qty,product.getPrice());
  }
  return orders.save(order);
 }
}