package cl.oyl.catalog;

import jakarta.persistence.*;

@Entity @Table(name = "products")
public class Product {
    @Id private String id;
    private String name;
    private String category;
    private String description;
    private String presentation;
    private int price;
    private int stock;
    private boolean active;
    private String imageUrl;

    public Product() {}
    public String getId() { return id; }
    public void setId(String v) { id = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public String getCategory() { return category; }
    public void setCategory(String v) { category = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public String getPresentation() { return presentation; }
    public void setPresentation(String v) { presentation = v; }
    public int getPrice() { return price; }
    public void setPrice(int v) { price = v; }
    public int getStock() { return stock; }
    public void setStock(int v) { stock = v; }
    public boolean isActive() { return active; }
    public void setActive(boolean v) { active = v; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String v) { imageUrl = v; }
}
