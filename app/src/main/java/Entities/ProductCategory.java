package Entities;

public class ProductCategory {
    private String id;
    private String name;
    private String description;

    // Default constructor required for calls to DataSnapshot.getValue(ProductCategory.class)
    public ProductCategory() {}

    // Parameterized constructor
    public ProductCategory(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
