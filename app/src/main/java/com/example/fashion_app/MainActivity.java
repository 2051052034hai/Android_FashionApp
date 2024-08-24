package com.example.fashion_app;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.widget.SearchView;

import android.util.Log;
import android.widget.EditText;
import android.content.Intent;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Adapter.ProductAdapter;
import Common.BaseActivity;
import Common.CartManager;
import Common.CartUpdateListener;
import Entities.Product;
import Entities.User;

public class MainActivity extends BaseActivity implements CartUpdateListener {
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private SearchView searchView;
    private EditText editText;
    private DatabaseReference productsRef;
    private List<Product> productList;
    private CartManager cartManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cartManager = new CartManager(this);
        cartManager = CartManager.getInstance(this);
        cartManager.setCartUpdateListener(this);
        //Set up RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.requestFocus(); // Đặt focus vào RecyclerView

        User session = User.getInstance();
        String email = session.getEmail();
        String username = session.getUserName();
        String userID = session.getId();

        // Initialize Firebase Database reference
        productsRef = FirebaseDatabase.getInstance().getReference("products");

        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);

        // Fetch products from Firebase
        fetchProductsFromFirebase();

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


    private void fetchProductsFromFirebase() {
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        productList.add(product);
                    }
                }
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
                Log.e("MainActivity", "Failed to fetch data", databaseError.toException());
            }
        });
    }

    // Trả về layout ToolBar cho màn hình
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main; // Trả về layout của activity
    }

    @Override
    protected void onResume() {
        super.onResume();
        cartManager.setCartUpdateListener(this);
        cartManager.notifyCartUpdated();
    }
}
