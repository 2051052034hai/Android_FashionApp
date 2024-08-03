package com.example.fashion_app;

import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.widget.SearchView;

import android.widget.EditText;
import android.content.Intent;

import Adapter.ProductAdapter;
import Common.BaseActivity;
import Entities.Product;

public class MainActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private SearchView searchView;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.requestFocus(); // Đặt focus vào RecyclerView

        List<Product> productList = new ArrayList<>();
        productList.add(new Product(1, R.drawable.product_1, "Áo Thun Nam", "200.000 vnđ"));
        productList.add(new Product(2, R.drawable.product_2, "Áo Thun Nữ", "150.000 vnđ"));
        productList.add(new Product(3, R.drawable.product_1, "Áo Thun Nam", "200.000 vnđ"));
        productList.add(new Product(4, R.drawable.product_2, "Áo Thun Nữ", "150.000 vnđ"));
        productList.add(new Product(5, R.drawable.product_1, "Áo Thun Nam", "200.000 vnđ"));
        productList.add(new Product(6, R.drawable.product_2, "Áo Thun Nữ", "150.000 vnđ"));
        productList.add(new Product(7, R.drawable.product_1, "Áo Thun Nam", "200.000 vnđ"));
        productList.add(new Product(8, R.drawable.product_2, "Áo Thun Nữ", "150.000 vnđ"));

        productAdapter = new ProductAdapter(productList);
        productAdapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Product product) {
                Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
                intent.putExtra("PRODUCT_ID", product.getId());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(productAdapter);

    }


    // Trả về layout ToolBar cho màn hình
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main; // Trả về layout của activity
    }

}
