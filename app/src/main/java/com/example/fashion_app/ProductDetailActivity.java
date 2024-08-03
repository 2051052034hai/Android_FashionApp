package com.example.fashion_app;
import android.os.Bundle;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import android.view.View;
import android.widget.Toast;
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

        int productId = getIntent().getIntExtra("PRODUCT_ID", -1);

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

        relatedProductList.add(new Product(1, R.drawable.product_1, "Áo Len Nữ", "150.000 vnđ"));
        relatedProductList.add(new Product(2, R.drawable.product_2,"Áo Khoác Nam", "250.000 vnđ"));

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
