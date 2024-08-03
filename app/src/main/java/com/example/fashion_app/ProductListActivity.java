package com.example.fashion_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import Adapter.ProductListAdapter;
import Entities.Product;

public class ProductListActivity extends AppCompatActivity {
    private RecyclerView recyclerViewProductList;
    private ProductListAdapter productListAdapter;
    private ImageView btnAddNew;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_management);

        recyclerViewProductList = findViewById(R.id.recycler_view_productList);

        recyclerViewProductList.setLayoutManager(new LinearLayoutManager(this));  // Set LayoutManager

        List<Product> productList = new ArrayList<>();
        productList.add(new Product(1, R.drawable.product_1, "Áo Thun Nam", "200.000 VND"));
        productList.add(new Product(2, R.drawable.product_2, "Áo Thun Nữ", "150.000 VND"));
        productList.add(new Product(3, R.drawable.product_1, "Áo Thun Nam", "200.000 VND"));
        productList.add(new Product(4, R.drawable.product_2, "Áo Thun Nữ", "150.000 VND"));
        productList.add(new Product(5, R.drawable.product_1, "Áo Thun Nam", "200.000 VND"));
        productList.add(new Product(6, R.drawable.product_2, "Áo Thun Nữ", "150.000 VND"));

        productListAdapter = new ProductListAdapter(productList, this);
        recyclerViewProductList.setAdapter(productListAdapter);

       // Xử lý khi click vào button thêm mới
        btnAddNew = findViewById(R.id.btn_delete_item);
        btnAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductListActivity.this, AddProduct.class);
                startActivity(intent);
            }
        });
    }
}
