package Common;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;

import com.example.fashion_app.CartActivity;
import com.example.fashion_app.LoginActivity;
import com.example.fashion_app.MainActivity;
import com.example.fashion_app.ProductListActivity;
import com.example.fashion_app.R;
import com.example.fashion_app.RegisterActivity;

import Entities.User;


public abstract class BaseActivity extends AppCompatActivity implements CartUpdateListener{
    private CartManager cartManager;
    private boolean isMenuCreated = false;
    private TextView cartBadge;
    private User userSession;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
        cartManager = CartManager.getInstance(this);
        cartManager.setCartUpdateListener(this);
    }

    @Override
    public void onCartUpdated(int itemCount) {
        if (isMenuCreated) {
            runOnUiThread(() -> updateCartBadge(itemCount));
        }
    }

    private void updateCartBadge(int itemCount) {
        if (cartBadge != null) {
            if (itemCount > 0) {
                cartBadge.setVisibility(View.VISIBLE);
                cartBadge.setText(String.valueOf(itemCount));
            } else {
                cartBadge.setText("0");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        resizeMenuItemIcons(menu); // Call the method to resize the menu item icons

        //Lấy thông tin user đang đăng nhập
        userSession = User.getInstance();
        String userID = userSession.getId();
        int roleUser = userSession.getRole();

        MenuItem searchItem = menu.findItem(R.id.action_search);
        MenuItem cartItem = menu.findItem(R.id.action_cart);
        MenuItem logAdminItem = menu.findItem(R.id.action_logAdmin);
        MenuItem signInItem = menu.findItem(R.id.action_signIn);
        MenuItem signUpItem = menu.findItem(R.id.action_signUp);
        MenuItem logoutItem = menu.findItem(R.id.action_logout);

        //Xử lý ẩn hiện các item dropdown khi đăng nhập
        if(userID == null){
            signInItem.setVisible(true);
            signUpItem.setVisible(true);
            logoutItem.setVisible(false);
        }else {
            signInItem.setVisible(false);
            signUpItem.setVisible(false);
            logoutItem.setVisible(true);
        }

        if(userID == null && roleUser == 2){
            logAdminItem.setVisible(true);
        }
        else{
            logAdminItem.setVisible(false);
        }

        View actionView = cartItem.getActionView();
        if (actionView != null) {
            cartBadge = actionView.findViewById(R.id.cart_badge);
            ImageView cartIcon = actionView.findViewById(R.id.cart_icon);

            // Set up click listener for the cart icon
            cartIcon.setOnClickListener(v -> {
                // Handle cart icon click
                onOptionsItemSelected(cartItem);
            });

            isMenuCreated = true;
            // Ensure the badge is updated
            onCartUpdated(cartManager.getCartItemCount());
        }

        androidx.appcompat.widget.SearchView searchView = (SearchView) searchItem.getActionView();
        // Thiết lập trình lắng nghe thay đổi tiêu điểm cho SearchView
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Ẩn biểu tượng giỏ hàng khi SearchView lấy được tiêu điểm
                if (cartItem != null) {
                    cartItem.setVisible(false);
                }
            } else {
                // Tùy chọn, hiển thị biểu tượng giỏ hàng khi SearchView mất tiêu điểm
                if (cartItem != null) {
                    cartItem.setVisible(true);
                }
            }
        });

        // Xử lý tùy chọn thiết lập độ rộng SearchView khi mở rộng
        searchItem.getActionView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // Điều chỉnh độ rộng ở đây khi bố cục SearchView thay đổi

            }
        });

        return true;
    }

    //Hàm xử lý thay đổi kích thước các phần từ của Menu
    private void resizeMenuItemIcons(Menu menu) {
        int color = ContextCompat.getColor(this, R.color.white);

        MenuItem menuItem = menu.findItem(R.id.action_user);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.user);
        if (drawable != null) {
            menuItem.setIcon(resizeDrawable(drawable, 60, 60, color)); // Adjust the size as needed
        }

        menuItem = menu.findItem(R.id.action_cart);
        drawable = ContextCompat.getDrawable(this, R.drawable.cart);
        if (drawable != null) {
            menuItem.setIcon(resizeDrawable(drawable, 60, 60, color)); // Adjust the size as needed
        }

//        menuItem = menu.findItem(R.id.action_prev);
//        drawable = ContextCompat.getDrawable(this, R.drawable.ic_prev);
//        if (drawable != null) {
//            menuItem.setIcon(resizeDrawable(drawable, 60, 60, color)); // Adjust the size as needed
//        }

        // Lặp lại cho các mục menu khác nếu cần
    }

    //Hàm xử lý thay đổi màu sắc, kích cỡ cho các icon trên ToolBar Menu
    private Drawable resizeDrawable(Drawable drawable, int width, int height, int color) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        drawable.draw(canvas);
        return new BitmapDrawable(getResources(), bitmap);
    }

    //Hàm xử lý khi click vào vào các icon trên ToolBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_user:
                return true;
            case R.id.action_cart:
                // Chuyển đến CartActivity khi nhấp vào action_cart
                Intent intent = new Intent(this, CartActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_logAdmin:
                //Chuyển đến productListActivity khi nhấp vào action_logAdmin
                intent = new Intent(this, ProductListActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_logout:
                //Chuyển đến LoginActivity khi nhấp vào action_logout
                //Retset thông tin User
                userSession = User.getInstance();
                userSession.setEmail(null);
                userSession.setUserName(null);
                userSession.setId(null);
                userSession.setRole(0);
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_signIn:
                //Chuyển đến trang Login
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_signUp:
                //Chuyển đến trang Register
                intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cartManager.notifyCartUpdated();
    }
    protected abstract int getLayoutResourceId();
}
