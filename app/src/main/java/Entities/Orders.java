package Entities;

import java.util.List;
import java.util.Map;

public class Orders {
    private String id;
    private double totalAmount;
    private String userId;
    private Map<String, Product> items;

    // Default constructor
    public Orders() {
    }

    // Parameterized constructor
    public Orders(String id, double totalAmount, String userId, Map<String, Product> items) {
        this.id = id;
        this.totalAmount = totalAmount;
        this.userId = userId;
        this.items = items;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, Product> getItems() {
        return items;
    }

    public void setItems(Map<String, Product> items) {
        this.items = items;
    }
}
