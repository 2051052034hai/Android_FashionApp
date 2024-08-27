package com.example.fashion_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import Adapter.ProductListAdapter;
import Common.DrawerLayoutActivity;
import Entities.Product;

public class ProductListActivity extends DrawerLayoutActivity {
    private RecyclerView recyclerViewProductList;
    private ProductListAdapter productListAdapter;
    private ImageView btnAddNew;
    private DatabaseReference databaseReference;
    private List<Product> productList;

    private SearchView search_view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_product_management, findViewById(R.id.content_frame));

        recyclerViewProductList = findViewById(R.id.recycler_view_productList);
        recyclerViewProductList.setLayoutManager(new LinearLayoutManager(this));

        productList = new ArrayList<>();
        productListAdapter = new ProductListAdapter(productList, this, 0);
        recyclerViewProductList.setAdapter(productListAdapter);

        //Load danh sách sản phẩm
        settingAllProductsFromFirebase();

       // Xử lý khi click vào button thêm mới
        btnAddNew = findViewById(R.id.btn_addnew_item);
        btnAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductListActivity.this, AddProductActivity.class);
                startActivity(intent);
            }
        });

        // Xử lý khi click vào nút search sản phẩm
        search_view = findViewById(R.id.search_view);
        search_view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    // If the search view is cleared, show all products
                    settingAllProductsFromFirebase();
                } else {
                    // Filter product list based on search text
                    filterProductList(newText);
                }
                return true;
            }
        });

    }

    //Hàm xử lý lọc sản phẩm theo tên
    private void filterProductList(String query) {
        List<Product> filteredList = new ArrayList<>();

        // Normalize query để không phân biệt dấu và chữ hoa thường
        String normalizedQuery = normalizeString(query.toLowerCase());

        for (Product product : productList) {
            // Normalize tên sản phẩm để so sánh
            String normalizedProductName = normalizeString(product.getName().toLowerCase());

            if (normalizedProductName.contains(normalizedQuery)) {
                filteredList.add(product);
            }
        }
        productListAdapter.updateList(filteredList);
    }

    // Hàm để loại bỏ dấu tiếng Việt
    private String normalizeString(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }

    //Xử lý load thông tin cho danh muc loại sản phẩm
    private void settingAllProductsFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("products");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null) {
                        productList.add(product);
                    }
                }
                productListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductListActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
