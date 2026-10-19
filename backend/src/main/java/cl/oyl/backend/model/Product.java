package cl.oyl.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity @Table(name="products")
public class Product {
 @Id private String id;
 @NotBlank private String name;
 @NotBlank private String category;
 private String description;
 private String presentation;
 @Min(0) private int price;
 @Min(0) private int stock;
 private boolean active;
 protected Product() {}
 public Product(String id,String name,String category,String description,String presentation,int price,int stock,boolean active) {
  this.id=id;this.name=name;this.category=category;this.description=description;this.presentation=presentation;
  this.price=price;this.stock=stock;this.active=active;
 }
 public String getId(){return id;}
 public String getName(){return name;}
 public String getCategory(){return category;}
 public String getDescription(){return description;}
 public String getPresentation(){return presentation;}
 public int getPrice(){return price;}
 public int getStock(){return stock;}
 public boolean isActive(){return active;}
 public void setName(String value){name=value;}
 public void setCategory(String value){category=value;}
 public void setDescription(String value){description=value;}
 public void setPresentation(String value){presentation=value;}
 public void setPrice(int value){price=value;}
 public void setStock(int value){stock=value;}
 public void setActive(boolean value){active=value;}
}