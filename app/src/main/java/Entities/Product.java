package Entities;

public class Product {
    private String id;
    private String name;
    private String categoryId;
    private String description;
    private String price;
    private long stock;
    private String imageUrl;

    // Default constructor required for calls to DataSnapshot.getValue(Product.class)
    public Product() {}

    // Parameterized constructor
    public Product(String id, String name, String categoryId, String description, String price, long stock, String imageUrl) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
    }

    // Getters and setters for each field
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public long getStock() {
        return stock;
    }

    public void setStock(long stock) {
        this.stock = stock;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
