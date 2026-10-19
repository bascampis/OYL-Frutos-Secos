package cl.oyl.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity @Table(name="order_lines")
public class OrderLine {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) @JsonIgnore private OrderHeader order;
 private String productId;
 private String productName;
 private int quantity;
 private int unitPrice;
 protected OrderLine(){}
 OrderLine(OrderHeader order,String productId,String productName,int quantity,int unitPrice){
  this.order=order;this.productId=productId;this.productName=productName;this.quantity=quantity;this.unitPrice=unitPrice;
 }
 public Long getId(){return id;}
 public String getProductId(){return productId;}
 public String getProductName(){return productName;}
 public int getQuantity(){return quantity;}
 public int getUnitPrice(){return unitPrice;}
}