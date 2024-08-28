package com.example.fashion_app;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import Common.DrawerLayoutActivity;
import Entities.Product;
import Entities.ProductCategory;

public class AddProductActivity  extends DrawerLayoutActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private TextInputEditText productName, productPrice, productStock, productDescription, productDiscount;
    private Button btnProductImage, btnSubmit;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private DatabaseReference categoriesReference;
    private TextView txtViewUrlImg;
    private Uri imageUri;
    private String productId;
    private boolean isEditMode = false;
    private String fileNameImg;
    private String oldImageUrl;
    private ImageView imgProductThumbnail;
    private Spinner spinnerProductCategory;
    private String selectedCategoryId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_add_product, findViewById(R.id.content_frame));

        // Initialize Firebase references
        databaseReference = FirebaseDatabase.getInstance().getReference("products");
        storageReference = FirebaseStorage.getInstance().getReference("product_images");
        categoriesReference = FirebaseDatabase.getInstance().getReference("categories");

        // Initialize views
        productName = findViewById(R.id.ProductName);
        productPrice = findViewById(R.id.ProductPrice);
        productStock = findViewById(R.id.ProductStock);
        productDescription = findViewById(R.id.ProductDescription);
        productDiscount = findViewById(R.id.productDiscount);
        btnProductImage = findViewById(R.id.btnProdutImage);
        btnSubmit = findViewById(R.id.btnSubmit);
        txtViewUrlImg = findViewById(R.id.txtViewUrlImg);
        spinnerProductCategory = findViewById(R.id.spinnerProductCategory);
        productDiscount.setText("0");

        // Kiểm tra nếu là trường hợp cập nhật
        Intent intent = getIntent();
        if (intent.hasExtra("PRODUCT_ID")) {
            productId = intent.getStringExtra("PRODUCT_ID");
            loadProductData(productId);
            btnSubmit.setText("Cập nhật");
        }

        // Lấy dữ liệu từ Firebase và nạp vào Spinner
        categoriesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> categoryNames = new ArrayList<>();
                List<ProductCategory> categories = new ArrayList<>();

                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    ProductCategory category = categorySnapshot.getValue(ProductCategory.class);
                    categories.add(category);
                    categoryNames.add(category.getName()); // Lấy tên của loại sản phẩm để nạp vào Spinner
                }

                // Tạo Adapter và nạp dữ liệu vào Spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddProductActivity.this, android.R.layout.simple_spinner_item, categoryNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerProductCategory.setAdapter(adapter);

                // Thiết lập sự kiện chọn cho Spinner
                spinnerProductCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // Lưu ID của loại sản phẩm được chọn
                        selectedCategoryId = categories.get(position).getId();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Xử lý khi không có lựa chọn nào được chọn
                        selectedCategoryId = null;
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AddProductActivity.this, "Không thể tải dữ liệu loại sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });

        //Xử lý khi click vào button thêm mới sản phẩm
        btnSubmit.setOnClickListener(v -> {
            if (validateInputs()) {
                if (productId != null) {
                    if (imageUri != null) {
                        deleteOldImageAndUploadNew();
                    } else {
                        updateProductData(oldImageUrl);
                    }
                } else {
                    if (imageUri != null) {
                        uploadImageToFirebase();
                    } else {
                        Toast.makeText(AddProductActivity.this, "Xin hãy chọn một hình ảnh", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnProductImage.setOnClickListener(v -> openFileChooser());
    }

    //Hàm xử lý load dữ liệu sản phẩm khi cập nhật sản phẩm
    private void loadProductData(String productId) {
        DatabaseReference productRef = databaseReference.child(productId);
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Product product = dataSnapshot.getValue(Product.class);
                    if (product != null) {
                        productName.setText(product.getName());
                        productPrice.setText(String.valueOf(product.getPrice()));
                        productStock.setText(String.valueOf(product.getStock()));
                        productDescription.setText(product.getDescription());
                        txtViewUrlImg.setText(product.getImageUrl());
                        // Lấy URL của ảnh từ product
                        if(product.getImageUrl() != null)
                        {
                            String imageUrl = product.getImageUrl();
                            fileNameImg = Uri.parse(imageUrl).getLastPathSegment();
                            txtViewUrlImg.setText(fileNameImg != null ? fileNameImg : imageUrl);
                            imgProductThumbnail = findViewById(R.id.imgProductThumbnail);
                            // Sử dụng Glide để tải hình ảnh từ URL
                            Glide.with(AddProductActivity.this)
                                    .load(product.getImageUrl())
                                    .placeholder(R.drawable.white_product)
                                    .error(R.drawable.white_product)
                                    .into(imgProductThumbnail);
                        }

                    }
                } else {
                    Toast.makeText(AddProductActivity.this, "Product not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AddProductActivity.this, "Failed to load product data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Hàm xử lý khi bấm nút chọn hình ảnh từ thiết bị
    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    String fileName = getFileName(imageUri);
                    txtViewUrlImg.setText(fileName);
                }
            }
    );

    //Hàm xử lý khi bấm nút chọn hình ảnh từ thiết bị
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activityResultLauncher.launch(intent);
    }

    //Hàm xử lý khi ubload ảnh thành công từ thiết bị
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            String fileName = getFileName(imageUri);
            txtViewUrlImg.setText(fileName);
            oldImageUrl = fileName;
        }
        else if(data != null && data.getData() != null){
            imgProductThumbnail = findViewById(R.id.imgProductThumbnail);
            imageUri = data.getData();
            // Sử dụng Glide để tải hình ảnh từ URL
            Glide.with(AddProductActivity.this)
                    .load(imageUri)
                    .placeholder(R.drawable.white_product)
                    .error(R.drawable.white_product)
                    .into(imgProductThumbnail);
        }
    }

    //Xử lý xoá ảnh cũ khỏi firebase khi người dùng cập nhật sản phẩm
    private void deleteOldImageAndUploadNew() {
        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            StorageReference oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageUrl);
            oldImageRef.delete().addOnSuccessListener(aVoid -> uploadImageToFirebase())
                    .addOnFailureListener(e -> {
                        Toast.makeText(AddProductActivity.this, "Failed to delete old image", Toast.LENGTH_SHORT).show();
                        uploadImageToFirebase(); // Continue with the new upload anyway
                    });
        } else {
            uploadImageToFirebase();
        }
    }

    //Hàm xử lý lấy tên ảnh
    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            }
        } else if ("file".equals(uri.getScheme())) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    //Hàm xử lý get đường dẫn ảnh khi upload
    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(uri));
    }

    //Hàm xử lý upload ảnh sản phẩm
    private void uploadImageToFirebase() {
        if (imageUri != null) {
            String fileExtension = getFileExtension(imageUri);
            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + fileExtension);

            UploadTask uploadTask = fileReference.putFile(imageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    saveProductData(imageUrl);
                }).addOnFailureListener(e -> {
                    Toast.makeText(AddProductActivity.this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(AddProductActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            });
        }
    }

    //Hàm xử lý tạo mới/ cập nhật sản phẩm
    private void saveProductData(String imageUrl) {
        String name = Objects.requireNonNull(productName.getText()).toString();
        Double price =  Double.parseDouble(productPrice.getText().toString());
        long stock = Objects.requireNonNull(Long.parseLong(productStock.getText().toString()));
        String description = Objects.requireNonNull(productDescription.getText()).toString();
        Double discount = Double.parseDouble(productDiscount.getText().toString());

        // Kiểm tra nếu productId đã tồn tại (chế độ cập nhật) hoặc không (chế độ thêm mới)
        String productId = this.productId != null ? this.productId : databaseReference.push().getKey();

        if(discount == null || productDiscount.getText().toString().isEmpty()){
            discount = 0.0;
        }

        // Tạo một map để chứa dữ liệu
        Map<String, Object> productData = new HashMap<>();
        productData.put("name", name);
        productData.put("price", price);
        productData.put("stock", stock);
        productData.put("description", description);
        productData.put("discount", discount);
        productData.put("categoryId", selectedCategoryId);

        // Kiểm tra nếu đang trong chế độ cập nhật
        if (this.productId != null) {
            // Chỉ thêm imageUrl nếu người dùng đã chọn ảnh mới
            if (imageUrl != null && !imageUrl.isEmpty()) {
                productData.put("imageUrl", imageUrl);
            }
            // Thực hiện cập nhật dữ liệu
            databaseReference.child(productId).updateChildren(productData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Snackbar.make(findViewById(android.R.id.content), "Cập nhật sản phẩm thành công", Snackbar.LENGTH_LONG)
                                    .setAction("OK", v -> {
                                    })
                                    .show();
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), "Failed to update product", Snackbar.LENGTH_LONG)
                                    .setAction("RETRY", v -> {
                                    })
                                    .show();
                        }
                    });
        } else {
            // Nếu không có productId thì đây là thêm mới sản phẩm
            productData.put("id", productId);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                productData.put("imageUrl", imageUrl);
            }
            // Thực hiện thêm mới dữ liệu
            databaseReference.child(productId).setValue(productData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Đặt lại form chỉ khi thêm mới sản phẩm
                            resetForm();
                            Snackbar.make(findViewById(android.R.id.content), "Thêm mới sản phẩm thành công", Snackbar.LENGTH_LONG)
                                    .setAction("OK", v -> {
                                    })
                                    .show();
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), "Failed to save product", Snackbar.LENGTH_LONG)
                                    .setAction("RETRY", v -> {
                                    })
                                    .show();
                        }
                    });
        }
    }


    //Xử lý gọi hàm cập nhật sản phẩm
    private void updateProductData(String imageUrl) {
        saveProductData(imageUrl);
    }

    //Hàm xử lý reset form sau khi thêm mới sản phẩm
    private void resetForm() {
        productName.setText("");
        productPrice.setText("");
        productStock.setText("");
        productDescription.setText("");
        productDiscount.setText("");
        txtViewUrlImg.setText("0");
        imageUri = null;
        btnSubmit.setText("Thêm sản phẩm");
        imgProductThumbnail.setImageDrawable(null);
    }

    //Hàm xử lý check bắt buộc nhập cho các trường dữ liệu
    private boolean validateInputs() {
        boolean isValid = true;

        if (Objects.requireNonNull(productName.getText()).toString().trim().isEmpty()) {
            ((TextInputLayout) productName.getParent().getParent()).setError("Bạn phải nhập vào tên sản phẩm");
            isValid = false;
        } else {
            ((TextInputLayout) productName.getParent().getParent()).setError(null);
        }

        if (Objects.requireNonNull(productPrice.getText()).toString().trim().isEmpty()) {
            ((TextInputLayout) productPrice.getParent().getParent()).setError("Bạn phải nhâp vào giá sản phẩm");
            isValid = false;
        } else {
            ((TextInputLayout) productPrice.getParent().getParent()).setError(null);
        }

        if (Objects.requireNonNull(productStock.getText()).toString().trim().isEmpty()) {
            ((TextInputLayout) productStock.getParent().getParent()).setError("Bạn phải nhập vào số lượng tồn kho");
            isValid = false;
        } else {
            ((TextInputLayout) productStock.getParent().getParent()).setError(null);
        }

        if (Objects.requireNonNull(productDescription.getText()).toString().trim().isEmpty()) {
            ((TextInputLayout) productDescription.getParent().getParent()).setError("Bạn phải nhập vào mô tả sản phẩm");
            isValid = false;
        } else {
            ((TextInputLayout) productDescription.getParent().getParent()).setError(null);
        }

        if ( Double.parseDouble(productDiscount.getText().toString().trim()) > 100.00) {
            ((TextInputLayout) productDiscount.getParent().getParent()).setError("Bạn không thể nhập giảm giá lớn hơn 100%");
            isValid = false;
        } else {
            ((TextInputLayout) productDiscount.getParent().getParent()).setError(null);
        }

        return isValid;
    }
}
