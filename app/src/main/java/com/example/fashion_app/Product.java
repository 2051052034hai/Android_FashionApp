package com.example.fashion_app;

public class Product {
    private int imageResource;
    private String productName;
    private String productPrice;

    public Product(int imageResource, String productName, String productPrice) {
        this.imageResource = imageResource;
        this.productName = productName;
        this.productPrice = productPrice;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }
}
