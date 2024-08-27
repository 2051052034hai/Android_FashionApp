package com.example.fashion_app;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Common.DrawerLayoutActivity;
import Entities.Product;
import Entities.ProductCategory;

public class ViewProductActivity extends DrawerLayoutActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_view_product, findViewById(R.id.content_frame));

        // Lấy ID sản phẩm từ Intent
        String productId = getIntent().getStringExtra("PRODUCT_ID");

        // Lấy các view
        ImageView productImage = findViewById(R.id.productImage);
        TextView productName = findViewById(R.id.productName);
        TextView productPrice = findViewById(R.id.productPrice);
        TextView productDiscount = findViewById(R.id.productDiscount);
        TextView productStock = findViewById(R.id.productStock);
        TextView productDescription = findViewById(R.id.productDescription);
        TextView productCategory = findViewById(R.id.productCategory);

        // Truy vấn Firebase để lấy thông tin sản phẩm
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("products").child(productId);
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Product product = dataSnapshot.getValue(Product.class);

                // Cập nhật thông tin sản phẩm lên các view
                if (product != null) {
                    Glide.with(ViewProductActivity.this)
                            .load(product.getImageUrl())
                            .placeholder(R.drawable.white_product)
                            .error(R.drawable.white_product)
                            .into(productImage);

                    productName.setText(product.getName());
                    productPrice.setText(product.getPrice() + " VND");
                    productDiscount.setText(product.getDiscount() + " %");
                    productStock.setText(String.valueOf(product.getStock()));
                    productDescription.setText(product.getDescription());

                    // Truy vấn Firebase để lấy tên danh mục
                    DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("categories").child(product.getCategoryId());
                    categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot categorySnapshot) {
                            ProductCategory category = categorySnapshot.getValue(ProductCategory.class);
                            if (category != null) {
                                productCategory.setText(category.getName());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Xử lý lỗi nếu cần
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi
                Log.e("ViewProductActivity", "Error loading product", databaseError.toException());
            }
        });
    }
}


