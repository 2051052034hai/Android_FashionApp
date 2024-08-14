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
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        String productId = getIntent().getStringExtra("PRODUCT_ID");

        // Lấy các view
        ImageView productImage = findViewById(R.id.productImage);
        TextView productName = findViewById(R.id.productName);
        TextView productPrice = findViewById(R.id.productPrice);
        TextView productDescription = findViewById(R.id.productDescription);

        // Truy vấn Firebase để lấy thông tin sản phẩm
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("products").child(productId);
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Product product = dataSnapshot.getValue(Product.class);

                // Cập nhật thông tin sản phẩm lên các view
                if (product != null) {
                    Glide.with(ProductDetailActivity.this)
                            .load(product.getImageUrl())
                            .placeholder(R.drawable.white_product)
                            .error(R.drawable.white_product)
                            .into(productImage);

                    productName.setText(product.getName());
                    productPrice.setText(product.getPrice() + " VND");
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

        //Phần xử lý khi click thêm sản phẩm vào giỏ
        cartManager = new CartManager(this);
        addToCartButton = findViewById(R.id.addToCartButton);
        addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new cart item
                CartItem item = new CartItem("2", "Product Name", 1, 9.99);

                // Add the item to the cart
                cartManager.addToCart(item);

                // Optional: Notify the user
                Toast.makeText(ProductDetailActivity.this, "Added to cart", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Trả về layout ToolBar cho màn hình
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }
}
