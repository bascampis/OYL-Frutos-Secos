package cl.oyl.backend.web;

import cl.oyl.backend.model.*;
import cl.oyl.backend.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;
import java.util.List;

/** SOLO MVP DE LABORATORIO: sin autenticación remota. Nunca exponer este servicio a internet. */
@Service
public class AdminService {
 private final ProductRepository products;
 private final OrderRepository orders;
 public AdminService(ProductRepository products,OrderRepository orders) {this.products=products;this.orders=orders;}

 public record ProductInput(String id,String name,String category,String description,String presentation,int price,int stock,boolean active){}
 public record StockInput(int delta){}
 public record StatusInput(String status){}

 @Transactional
 public Product save(ProductInput input) {
  if(input.id()==null || !input.id().matches("[A-Za-z0-9_-]{3,24}") ||
     input.name()==null || input.name().isBlank() || input.category()==null || input.category().isBlank() ||
     input.price()<0 || input.stock()<0)
   throw new ResponseStatusException(BAD_REQUEST,"Datos inválidos");
  var found=products.lockById(input.id());
  Product p;
  if(found.isPresent()) {
   p=found.get();
   p.setName(input.name());p.setCategory(input.category());p.setDescription(input.description());
   p.setPresentation(input.presentation());p.setPrice(input.price());p.setActive(input.active());
   // Al editar producto no modificar stock: utilizar ajuste de inventario dedicado.
  } else p=new Product(input.id(),input.name(),input.category(),input.description(),
   input.presentation(),input.price(),input.stock(),input.active());
  return products.save(p);
 }
 @Transactional
 public Product adjust(String id,int delta) {
  Product p=products.lockById(id).orElseThrow(()->new ResponseStatusException(NOT_FOUND,"Producto no encontrado"));
  long next=(long)p.getStock()+delta;
  if(next<0 || next>Integer.MAX_VALUE) throw new ResponseStatusException(CONFLICT,"Stock inválido");
  p.setStock((int)next);
  return products.save(p);
 }
 @Transactional
 public OrderHeader status(String id,String status) {
  if(!List.of("Pendiente de Validación","Confirmado","Preparado","Entregado").contains(status))
   throw new ResponseStatusException(BAD_REQUEST,"Estado no permitido");
  OrderHeader order=orders.findById(id).orElseThrow(()->new ResponseStatusException(NOT_FOUND,"Pedido no encontrado"));
  order.setStatus(status);
  return orders.save(order);
 }
}