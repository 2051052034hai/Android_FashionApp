package com.example.fashion_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import Common.DrawerLayoutActivity;
import Entities.ProductCategory;

public class AddProductCategoryActivity extends DrawerLayoutActivity {
    private TextInputEditText productCatName, productCatDescription;
    private DatabaseReference databaseReference;
    private Button btnSubmit;
    private String productCatId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_add_product_category, findViewById(R.id.content_frame));

        // Initialize Firebase references
        databaseReference = FirebaseDatabase.getInstance().getReference("categories");

        // Initialize views
        productCatName = findViewById(R.id.ProductCatName);
        productCatDescription = findViewById(R.id.ProductCatDescription);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Kiểm tra nếu là trường hợp cập nhật
        Intent intent = getIntent();
        if (intent.hasExtra("PRODUCTCAT_ID") && intent.getStringExtra("PRODUCTCAT_ID") != null) {
            productCatId = intent.getStringExtra("PRODUCTCAT_ID");
            loadProductData(productCatId);
            btnSubmit.setText("Cập nhật");
        }

        //Xử lý khi click vào button thêm mới sản phẩm
        btnSubmit.setOnClickListener(v -> {
            if (validateInputs()) {
                saveProductData();
            }
        });

    }

    //Hàm xử lý load dữ liệu sản phẩm khi cập nhật sản phẩm
    private void loadProductData(String productCatId) {
        DatabaseReference productRef = databaseReference.child(productCatId);
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ProductCategory productCat = dataSnapshot.getValue(ProductCategory.class);
                    if (productCat != null) {
                        productCatName.setText(productCat.getName());
                        productCatDescription.setText(productCat.getDescription());
                    }
                } else {
                    Toast.makeText(AddProductCategoryActivity.this, "ProductCat not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AddProductCategoryActivity.this, "Failed to load productCat data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Hàm xử lý tạo mới/ cập nhật sản phẩm
    private void saveProductData() {
        String name = Objects.requireNonNull(productCatName.getText()).toString();
        String description = Objects.requireNonNull(productCatDescription.getText()).toString();

        // Kiểm tra nếu productCatId đã tồn tại (chế độ cập nhật) hoặc không (chế độ thêm mới)
        String productCatId = this.productCatId != null ? this.productCatId : databaseReference.push().getKey();

        // Tạo một map để chứa dữ liệu
        Map<String, Object> productData = new HashMap<>();

        // Nếu là trường hợp thêm mới, thêm id vào dữ liệu
        if (this.productCatId == null) {
            productData.put("id", productCatId);
        }

        productData.put("name", name);
        productData.put("description", description);

        // Chọn phương thức lưu trữ phù hợp (cập nhật hoặc thêm mới)
        Task<Void> saveTask = this.productCatId != null ?
                databaseReference.child(productCatId).updateChildren(productData) :
                databaseReference.child(productCatId).setValue(productData);

        saveTask.addOnCompleteListener(task -> {
            String message = task.isSuccessful() ?
                    (this.productCatId != null ? "Cập nhật loại sản phẩm thành công" : "Thêm mới loại sản phẩm thành công") :
                    (this.productCatId != null ? "Failed to update product category" : "Failed to save product");

            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                    .setAction(task.isSuccessful() ? "OK" : "RETRY", v -> {
                    })
                    .show();

            if (task.isSuccessful() && this.productCatId == null) {
                resetForm();
            }
        });
    }


    //Hàm xử lý reset form sau khi thêm mới loại sản phẩm
    private void resetForm() {
        productCatName.setText("");
        productCatDescription.setText("");
        productCatId = null;
        btnSubmit.setText("Thêm loại sản phẩm");
    }

    //Hàm xử lý check bắt buộc nhập cho các trường dữ liệu
    private boolean validateInputs() {
        boolean isValid = true;

        if (Objects.requireNonNull(productCatName.getText()).toString().trim().isEmpty()) {
            ((TextInputLayout) productCatName.getParent().getParent()).setError("Bạn phải nhập vào tên loại sản phẩm");
            isValid = false;
        } else {
            ((TextInputLayout) productCatName.getParent().getParent()).setError(null);
        }

        if (Objects.requireNonNull(productCatDescription.getText()).toString().trim().isEmpty()) {
            ((TextInputLayout) productCatDescription.getParent().getParent()).setError("Bạn phải nhập vào mô tả sản phẩm");
            isValid = false;
        } else {
            ((TextInputLayout) productCatDescription.getParent().getParent()).setError(null);
        }

        return isValid;
    }
}
