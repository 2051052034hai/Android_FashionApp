package com.example.fashion_app;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import Common.BaseActivity;
import Common.CartManager;
import Adapter.CartAdapter;
import Entities.CartItem;

import java.util.List;
public class CartActivity extends BaseActivity implements CartAdapter.OnQuantityChangeListener{

    private ListView cartListView;
    private TextView totalTextView;
    private TextView cart_item_increase;
    private CartManager cartManager;
    private CartAdapter cartAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        settingDataCart();
        updateTotal();
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
            total += item.getPrice() * item.getQuantity();
        }
        totalTextView.setText(String.format("Tổng tiền: %.2fđ", total));
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

        cartAdapter = new CartAdapter(this, cartItems, cartManager,this);
        cartListView.setAdapter(cartAdapter);
    }
}
