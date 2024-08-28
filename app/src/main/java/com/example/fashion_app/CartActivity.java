package com.example.fashion_app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import Common.BaseActivity;
import Common.CartManager;
import Adapter.CartAdapter;
import Common.CartUpdateListener;
import Entities.CartItem;
import Entities.User;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class CartActivity extends BaseActivity implements CartAdapter.OnQuantityChangeListener{

    private ListView cartListView;
    private TextView totalTextView;
    private CartManager cartManager;
    private CartAdapter cartAdapter;
    private DatabaseReference databaseReference;
    private Button checkout_button;
    private CartUpdateListener cartUpdateListener;
    private User userSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        cartManager = CartManager.getInstance(this);
        cartManager.setCartUpdateListener(this);
        cartUpdateListener = this;

        databaseReference = FirebaseDatabase.getInstance().getReference("products");

        settingDataCart();
        updateTotal();

        checkout_button = findViewById(R.id.checkout_button);
        //Lấy thông tin user đang đăng nhập
        userSession = User.getInstance();
        String userID = userSession.getId();

        checkout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userID != null){
                    //Xử lý thanh toán đơn hàng
                    performCheckout();
                }
                else {
                    // Hiển thị một AlertDialog để yêu cầu đăng nhập
                    new AlertDialog.Builder(CartActivity.this)
                            .setTitle("Đăng nhập")
                            .setMessage("Để thanh toán sản phẩm trong giỏ, vui lòng đăng nhập !")
                            .setPositiveButton("OK", (dialog, which) -> {
                                Intent intent = new Intent(CartActivity.this, LoginActivity.class);
                                startActivity(intent);
                            })
                            .setNegativeButton("Không", (dialog, which) -> {
                                // Xử lý khi người dùng nhấn "Không"
                                dialog.dismiss();
                            })
                            .show();
                }

            }
        });
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

    //Hàm xử lý cập nhật lại giá trị tổng tiền giỏ hàng
    private void updateTotal() {
        List<CartItem> cartItems = cartManager.getCart();
        double total = 0;
        for (CartItem item : cartItems) {
            double itemProductPriceDiscount = item.getPrice() - (item.getPrice() * item.getDiscount() / 100);
            total += itemProductPriceDiscount * item.getQuantity();
        }
        totalTextView.setText(String.format("%s đ", NumberFormat.getInstance(new Locale("vi", "VN")).format(total)));
    }

    //Xử lý khi cập nhật số lượng sản phẩm trong giỏ
    @Override
    public void onQuantityChanged() {
        updateTotal();
        settingDataCart();
    }

    //Hàm thiết lập thông tin để render lại dữ liệu mỗi khi thao tác xóa với giỏ hàng
    public void settingDataCart(){
        cartListView = findViewById(R.id.cart_items_list_view);
        totalTextView = findViewById(R.id.cart_total);

        cartManager = new CartManager(this);
        List<CartItem> cartItems = cartManager.getCart();

        cartAdapter = new CartAdapter(this, cartItems, cartManager,this, this);
        cartListView.setAdapter(cartAdapter);
    }

    //Hàm xử lý thanh toán sản phẩm trong giỏ
    private void performCheckout() {
        final List<CartItem> cartItems = cartManager.getCart();
        if (cartItems.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "Giỏ hàng trống", Snackbar.LENGTH_LONG)
                    .setAction("OK", v -> {
                    })
                    .show();
            return;
        }

        final String orderId = UUID.randomUUID().toString();
        double totalAmount = 0;

        // Create a map to store order details
        Map<String, Object> order = new HashMap<>();
        order.put("id", orderId);
        order.put("userId", userSession.getId());
        order.put("items", new HashMap<String, Object>());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        order.put("createdDate", currentDate);

        List<DatabaseReference> productRefs = new ArrayList<>();
        List<Map<String, Object>> orderItems = new ArrayList<>();

        // Calculate total amount and prepare order items
        for (CartItem item : cartItems) {
            double itemProductPriceDiscount = item.getPrice() - (item.getPrice() * item.getDiscount() / 100);
            totalAmount += itemProductPriceDiscount * item.getQuantity();

            Map<String, Object> orderItem = new HashMap<>();
            orderItem.put("categoryId", item.getCategoryId());
            orderItem.put("description", item.getDescription());
            orderItem.put("imageUrl", item.getImageUrl());
            orderItem.put("id", item.getProductId());
            orderItem.put("name", item.getProductName());
            orderItem.put("price", item.getPrice());
            orderItem.put("discount", item.getDiscount());
            orderItem.put("quantity", item.getQuantity());

            orderItems.add(orderItem);
            productRefs.add(databaseReference.child(item.getProductId()).child("quantity"));
        }

        order.put("totalAmount", totalAmount);

        // Check product availability and update quantities
        databaseReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                boolean isOutOfStock = false;

                for (int i = 0; i < cartItems.size(); i++) {
                    CartItem item = cartItems.get(i);
                    MutableData productData = mutableData.child(item.getProductId()).child("stock");
                    Long currentQuantity = productData.getValue(Long.class);

                    if (currentQuantity == null) {
                        return Transaction.abort();
                    }

                    Long newQuantity = currentQuantity - item.getQuantity();
                    if (newQuantity < 0) {
                        isOutOfStock = true;
                        break;
                    }

                    productData.setValue(newQuantity);
                    ((Map<String, Object>) order.get("items")).put(item.getProductId(), orderItems.get(i));
                }

                if (isOutOfStock) {
                    return Transaction.abort();
                }

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    // Save the order to Firebase
                    DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);
                    orderRef.setValue(order).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Clear the cart and show success message
                            cartManager.clearCart();
                            cartAdapter.notifyDataSetChanged();
                            updateTotal();
                            settingDataCart();
                            cartManager.setCartUpdateListener(cartUpdateListener);
                            cartManager.notifyCartUpdated();
                            Snackbar.make(findViewById(android.R.id.content), "Đặt hàng thành công", Snackbar.LENGTH_LONG)
                                    .setAction("OK", v -> {
                                    })
                                    .show();
                        } else {
                            // Handle failure
                            Snackbar.make(findViewById(android.R.id.content), "Đặt hàng không thành công", Snackbar.LENGTH_LONG)
                                    .setAction("OK", v -> {
                                    })
                                    .show();
                        }
                    });
                } else {
                    // Handle out of stock case
                    Snackbar.make(findViewById(android.R.id.content), "Một hoặc nhiều sản phẩm hết hàng", Snackbar.LENGTH_LONG)
                            .setAction("OK", v -> {
                            })
                            .show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cartManager.setCartUpdateListener(this);
        cartManager.notifyCartUpdated();
    }

}
