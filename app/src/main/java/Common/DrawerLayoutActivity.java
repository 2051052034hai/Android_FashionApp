package Common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.fashion_app.OrdersListActivity;
import com.example.fashion_app.ProductCategoryListActivity;
import com.example.fashion_app.ProductListActivity;
import com.example.fashion_app.R;

public abstract class DrawerLayoutActivity extends AppCompatActivity {

    private ImageButton menuButton;
    private DrawerLayout drawerLayout;

    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_layout);
        drawerLayout = findViewById(R.id.drawer_layout);
        linearLayout = findViewById(R.id.menu_layout);

        menuButton = findViewById(R.id.menu_button);

        findViewById(R.id.menu_product_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(DrawerLayoutActivity.this, ProductListActivity.class));
                    }
                }, 250);
            }
        });

        findViewById(R.id.menu_category_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(DrawerLayoutActivity.this, ProductCategoryListActivity.class));
                    }
                }, 250);
            }
        });

        findViewById(R.id.menu_orders_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(DrawerLayoutActivity.this, OrdersListActivity.class));
                    }
                }, 250);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_Menu) {
            toggleMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleMenu() {
        if (drawerLayout.isDrawerOpen(linearLayout)) {
            drawerLayout.closeDrawer(linearLayout);
        } else {
            drawerLayout.openDrawer(linearLayout);
        }
    }

}
