package com.example.fashion_app;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.widget.SearchView;
import android.widget.EditText;
import android.util.TypedValue;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private SearchView searchView;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchView = findViewById(R.id.search_view);
        editText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.requestFocus(); // Đặt focus vào RecyclerView

        List<Product> productList = new ArrayList<>();
        productList.add(new Product(R.drawable.product_1, "Áo Thun Nam", "200.000 vnđ"));
        productList.add(new Product(R.drawable.product_2, "Áo Thun Nữ", "150.000 vnđ"));
        productList.add(new Product(R.drawable.product_1, "Áo Thun Nam", "200.000 vnđ"));
        productList.add(new Product(R.drawable.product_2, "Áo Thun Nữ", "150.000 vnđ"));

        productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);

    }
}
