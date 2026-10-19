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
 public StoreController(ProductRepository products,OrderRepository orders,OrderService service){
  this.products=products;this.orders=orders;this.service=service;
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
 @GetMapping("/health") public Map<String,String> health(){return Map.of("status","ok","mode","ACADEMIC_DEMO");}
}