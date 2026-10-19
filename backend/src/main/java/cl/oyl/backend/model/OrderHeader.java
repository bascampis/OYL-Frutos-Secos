package cl.oyl.backend.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity @Table(name="orders")
public class OrderHeader {
 @Id private String id;
 @Column(nullable=false) private String clientAlias;
 private Instant createdAt;
 private int total;
 private String status;
 private String fulfillment;
 @OneToMany(mappedBy="order",cascade=CascadeType.ALL,orphanRemoval=true,fetch=FetchType.EAGER)
 private List<OrderLine> lines=new ArrayList<>();
 protected OrderHeader(){}
 public OrderHeader(String clientAlias,String fulfillment) {
  id=UUID.randomUUID().toString();this.clientAlias=clientAlias;this.fulfillment=fulfillment;
  createdAt=Instant.now();status="Pendiente de Validación";
 }
 public void addLine(String productId,String productName,int quantity,int unitPrice) {
  lines.add(new OrderLine(this,productId,productName,quantity,unitPrice));
  total+=unitPrice*quantity;
 }
 public String getId(){return id;}
 public String getClientAlias(){return clientAlias;}
 public Instant getCreatedAt(){return createdAt;}
 public int getTotal(){return total;}
 public String getStatus(){return status;}
 public void setStatus(String value){status=value;}
 public String getFulfillment(){return fulfillment;}
 public List<OrderLine> getLines(){return lines;}
}