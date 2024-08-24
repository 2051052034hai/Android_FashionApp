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

import Adapter.ProductCategoryListAdapter;
import Common.DrawerLayoutActivity;
import Entities.ProductCategory;

public class ProductCategoryListActivity extends DrawerLayoutActivity {

    private RecyclerView recyclerViewProductCategoryList;
    private ProductCategoryListAdapter productCategoryListAdapter;
    private ImageView btnAddNew;
    private DatabaseReference databaseReference;
    private List<ProductCategory> productCategoryList;
    private SearchView search_view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_product_category_management, findViewById(R.id.content_frame));

        recyclerViewProductCategoryList = findViewById(R.id.recycler_view_productCategorydList);
        recyclerViewProductCategoryList.setLayoutManager(new LinearLayoutManager(this));

        productCategoryList = new ArrayList<>();
        productCategoryListAdapter = new ProductCategoryListAdapter(productCategoryList, this);
        recyclerViewProductCategoryList.setAdapter(productCategoryListAdapter);

        //Load danh sách loại sản phẩm
        settingAllCategoriesFromFirebase();

        // Xử lý khi click vào button thêm mới
        btnAddNew = findViewById(R.id.btn_addnew_item);
        btnAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductCategoryListActivity.this, AddProductCategoryActivity.class);
                startActivity(intent);
            }
        });

        // Xử lý khi click vào nút search loại sản phẩm
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
                    settingAllCategoriesFromFirebase();
                } else {
                    // Filter product list based on search text
                    filterProductCategoryList(newText);
                }
                return true;
            }
        });

    }

    //Hàm xử lý lọc loại sản phẩm theo tên
    private void filterProductCategoryList(String query) {
        List<ProductCategory> filteredList = new ArrayList<>();

        // Normalize query để không phân biệt dấu và chữ hoa thường
        String normalizedQuery = normalizeString(query.toLowerCase());

        for (ProductCategory productCat : productCategoryList) {
            // Normalize tên sản phẩm để so sánh
            String normalizedProductName = normalizeString(productCat.getName().toLowerCase());

            if (normalizedProductName.contains(normalizedQuery)) {
                filteredList.add(productCat);
            }
        }
        productCategoryListAdapter.updateList(filteredList);
    }

    // Hàm để loại bỏ dấu tiếng Việt
    private String normalizeString(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }

    //Xử lý load thông tin cho danh muc sản phẩm
    private void settingAllCategoriesFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("categories");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productCategoryList.clear();
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    ProductCategory productCat = productSnapshot.getValue(ProductCategory.class);
                    if (productCat != null) {
                        productCategoryList.add(productCat);
                    }
                }
                productCategoryListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductCategoryListActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
