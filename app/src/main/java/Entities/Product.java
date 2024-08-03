package Entities;

public class Product {
    private int ID;
    private int imageResource;
    private String productName;
    private String productPrice;

    private int StockQuantity;

    public Product(int ID, int imageResource, String productName, String productPrice) {
        this.ID = ID;
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

    //Lấy sản phẩm theo ID
    public int getId() {
        return ID;
    }

    public int getStockQuantity() {
        return StockQuantity;
    }
}
