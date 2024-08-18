package com.example.fashion_app;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Adapter.CommentAdapter;
import Adapter.RelatedProductAdapter;
import Common.BaseActivity;
import Entities.Comment;
import Entities.Product;
import Entities.CartItem;
import Common.CartManager;

public class ProductDetailActivity extends BaseActivity {

    private RecyclerView commentsRecyclerView;
    private RecyclerView relatedProductsRecyclerView;
    private CommentAdapter commentAdapter;
    private RelatedProductAdapter relatedProductAdapter;
    private List<Comment> commentList;
    private List<Product> relatedProductList;
    private Button addToCartButton;
    private CartManager cartManager;
    private Product product;
    private int currentQuantitySum = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        cartManager = CartManager.getInstance(this);
        cartManager.setCartUpdateListener(this);



        String productId = getIntent().getStringExtra("PRODUCT_ID");
        // Lấy các view
        ImageView productImage = findViewById(R.id.productImage);
        TextView productName = findViewById(R.id.productName);
        TextView productStock = findViewById(R.id.productStock);
        TextView productPrice = findViewById(R.id.productPrice);
        TextView productDescription = findViewById(R.id.productDescription);

        // Truy vấn Firebase để lấy thông tin sản phẩm
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("products").child(productId);
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                product = dataSnapshot.getValue(Product.class);

                // Cập nhật thông tin sản phẩm lên các view
                if (product != null) {
                    Glide.with(ProductDetailActivity.this)
                            .load(product.getImageUrl())
                            .placeholder(R.drawable.white_product)
                            .error(R.drawable.white_product)
                            .into(productImage);

                    productName.setText(product.getName());
                    productPrice.setText(product.getPrice() + " VND");
                    productStock.setText("Còn hàng: " + product.getStock() +" cái");
                    productDescription.setText(product.getDescription());
                }
            };
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi
                Log.e("ViewProductActivity", "Error loading product", databaseError.toException());
            }
        });

        //Ẩn title ToolBar
        //        if (getSupportActionBar() != null) {
        //            getSupportActionBar().setTitle("");
        //        }

        //Phần xử lý cho load thông tin bình luận, sản phẩm liên quan
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        relatedProductsRecyclerView = findViewById(R.id.relatedProductsRecyclerView);

        commentList = new ArrayList<>();
        relatedProductList = new ArrayList<>();

        // Populate the lists with data
        // Example data
        commentList.add(new Comment("John Doe", "Great product!"));
        commentList.add(new Comment("Jane Smith", "I really like it."));

//        relatedProductList.add(new Product(1, R.drawable.product_1, "Áo Len Nữ", "150.000 vnđ"));
//        relatedProductList.add(new Product(2, R.drawable.product_2,"Áo Khoác Nam", "250.000 vnđ"));

        commentAdapter = new CommentAdapter(commentList);
        relatedProductAdapter = new RelatedProductAdapter(relatedProductList);

        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentAdapter);

        relatedProductsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        relatedProductsRecyclerView.setAdapter(relatedProductAdapter);

        TextView itemIncrease = findViewById(R.id.product_item_increase);
        TextView itemDecrease = findViewById(R.id.product_item_decrease);
        final TextView quantityView = findViewById(R.id.product_item_quantity);
        currentQuantitySum = Integer.parseInt(quantityView.getText().toString());
        //Phần xử lý khi click vào nút tăng số lượng sản phẩm
        itemIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentQuantity = Integer.parseInt(quantityView.getText().toString());
                if(currentQuantity < product.getStock())
                {
                  quantityView.setText(String.valueOf(currentQuantity + 1));
                  currentQuantitySum = currentQuantity + 1;
                }else {
                    Snackbar.make(findViewById(android.R.id.content), "Bạn không thể mua quá số lượng tồn kho của sản phẩm!", Snackbar.LENGTH_LONG)
                            .setAction("OK", msg -> {
                            })
                            .show();
                }
            }
        });

        //Phần xử lý khi click vào nút giảm số lượng sản phẩm
        itemDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentQuantity = Integer.parseInt(quantityView.getText().toString());
                if(currentQuantity > 1) {
                    quantityView.setText(String.valueOf(currentQuantity - 1));
                    currentQuantitySum = currentQuantity - 1;
                }
            }
        });

        //Phần xử lý khi click thêm sản phẩm vào giỏ
        addToCartButton = findViewById(R.id.addToCartButton);
        addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new cart item
                CartItem item = new CartItem(
                        productId,
                        product.getName(),
                        currentQuantitySum,
                        Double.parseDouble(product.getPrice()),
                        product.getImageUrl()
                );

                // Add the item to the cart
                cartManager.addToCart(item);
                // Optional: Notify the user
                Snackbar.make(findViewById(android.R.id.content), "Thêm vào giỏ thành công", Snackbar.LENGTH_LONG)
                        .setAction("OK", msg -> {
                        })
                        .show();

            }
        });
    }

    // Trả về layout ToolBar cho màn hình
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onResume() {
        super.onResume();
        cartManager.setCartUpdateListener(this);
        cartManager.notifyCartUpdated();
    }
}
