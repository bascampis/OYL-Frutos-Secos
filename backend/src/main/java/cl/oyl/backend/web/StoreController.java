package cl.oyl.backend.web;

import cl.oyl.backend.model.*;
import cl.oyl.backend.repo.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController @RequestMapping("/api")
@CrossOrigin(origins="*")
public class StoreController {
 private final ProductRepository products;
 private final OrderRepository orders;
 private final OrderService service;
 private final AdminService admin;
 public StoreController(ProductRepository products,OrderRepository orders,OrderService service, AdminService admin){
  this.products=products;this.orders=orders;this.service=service;this.admin=admin;
 }
 @GetMapping("/products") public List<Product> products(){return products.findAll();}
 @GetMapping("/products/{id}") public Product product(@PathVariable String id) {
  return products.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
 }
 @PostMapping("/orders") @ResponseStatus(HttpStatus.CREATED)
 public OrderHeader order(@Valid @RequestBody OrderRequest request){return service.create(request);}
 @GetMapping("/orders") public List<OrderHeader> orders(@RequestParam String clientAlias) {
  return orders.findByClientAliasOrderByCreatedAtDesc(clientAlias);
 }
 // ENDPONTS ADMIN DE LABORATORIO: sin auth, usar solo en red local aislada.
 @PutMapping("/admin/products") public Product saveProduct(@RequestBody AdminService.ProductInput request) {
  return admin.save(request);
 }
 @PatchMapping("/admin/products/{id}/stock")
 public Product stock(@PathVariable String id, @RequestBody AdminService.StockInput request) {
  return admin.adjust(id,request.delta());
 }
 @GetMapping("/admin/orders") public List<OrderHeader> allOrders() {return orders.findAll();}
 @PatchMapping("/admin/orders/{id}/status")
 public OrderHeader status(@PathVariable String id,@RequestBody AdminService.StatusInput request) {
  return admin.status(id,request.status());
 }
 @GetMapping("/health") public Map<String,String> health(){return Map.of("status","ok","mode","ACADEMIC_DEMO");}
}