package Common;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import Entities.CartItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
public class CartManager {
    private static final String PREFS_NAME = "CART_PREFS";
    private static final String CART_KEY = "CART_ITEMS";
    private static CartManager instance;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    private CartUpdateListener cartUpdateListener;

    //Hàm khởi tạo quản lý giỏ hàng
    public CartManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context.getApplicationContext());
        }
        return instance;
    }

    //Hàm xử lý thêm một sản phẩm vào giỏ hàng
    public void addToCart(CartItem item) {
        List<CartItem> cart = getCart();
        boolean itemExists = false;

        for (CartItem itemCart : cart) {
            if (itemCart.getProductId().equals(item.getProductId())) {
                itemCart.setQuantity(itemCart.getQuantity() + item.getQuantity());
                itemExists = true;
                break;
            }
        }

        if (!itemExists) {
            cart.add(item);
        }

        saveCart(cart);
    }

    //Hàm lấy dữ liệu của giỏ hàng
    public List<CartItem> getCart() {
        String json = sharedPreferences.getString(CART_KEY, null);
        Type type = new TypeToken<List<CartItem>>() {}.getType();
        List<CartItem> cart = gson.fromJson(json, type);
        return cart == null ? new ArrayList<CartItem>() : cart;
    }

    //Hàm xử lý xóa tất cả sản phẩm trong giỏ
    public void clearCart() {
        saveCart(new ArrayList<CartItem>());
    }

    //Hàm xử lý lưu lại các thay đổi khi thao tác với giỏ hàng
    private void saveCart(List<CartItem> cart) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = gson.toJson(cart);
        editor.putString(CART_KEY, json);
        editor.apply();

        notifyCartUpdated();
    }

    //Hàm xử lý cập nhật số lượng của các sản phẩm trong giỏ
    public void updateCartItem(CartItem updatedItem) {
        List<CartItem> cart = getCart();
        for (CartItem item : cart) {
            if (item.getProductId().equals(item.getProductId())) {
                item.setQuantity(updatedItem.getQuantity());
                break;
            }
        }
        saveCart(cart);
    }

    //Hàm xử lý xóa các sản phẩm trong giỏ hàng
    public void handleDeleteCartItem(CartItem delItem) {
        List<CartItem> cart = getCart();
        for (CartItem item : cart) {
            if (item.getProductId().equals(delItem.getProductId())) {
                cart.remove(item);
                break;
            }
        }
        saveCart(cart);
    }

    public void setCartUpdateListener(CartUpdateListener listener) {
        this.cartUpdateListener = listener;
    }

    // Notify listener about cart update
    public void notifyCartUpdated() {
        if (cartUpdateListener != null) {
            cartUpdateListener.onCartUpdated(getCartItemCount());
        }
    }

    // Get the total number of items in the cart
    public int getCartItemCount() {
        return getCart().stream().mapToInt(CartItem::getQuantity).sum();
    }

}
