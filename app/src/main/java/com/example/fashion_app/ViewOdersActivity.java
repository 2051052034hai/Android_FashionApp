package com.example.fashion_app;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Adapter.ProductListAdapter;
import Common.DrawerLayoutActivity;
import Entities.Orders;
import Entities.Product;

public class ViewOdersActivity extends DrawerLayoutActivity {
    private RecyclerView recyclerViewProductList;
    private List<Product> productList;
    private ProductListAdapter productListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.item_orders_view, findViewById(R.id.content_frame));
        // Lấy ID sản phẩm từ Intent
        String orderId = getIntent().getStringExtra("ORDER_ID");
        String username = getIntent().getStringExtra("USERNAME");
        String createdDatetxt = getIntent().getStringExtra("CREATEDDATE");

        // Lấy các view
        TextView userName = findViewById(R.id.userName);
        TextView createdDate = findViewById(R.id.createdDate);
        TextView totalAmount = findViewById(R.id.totalAmount);

        // Truy vấn Firebase để lấy thông tin sản phẩm
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Orders orders = dataSnapshot.getValue(Orders.class);

                // Cập nhật thông tin sản phẩm lên các view
                if (orders != null) {
                    String formattedDateStr = formatCreatedDate(createdDatetxt);
                    userName.setText(username);
                    createdDate.setText(formattedDateStr);
                    NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);
                    String formattedTotalAmout = numberFormat.format(orders.getTotalAmount()) + " đ";
                    totalAmount.setText(String.valueOf(formattedTotalAmout));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi
                Log.e("ViewOdersActivity", "Error loading orders", databaseError.toException());
            }
        });

        //Xử lý load danh sách sản phẩm thuộc đơn hàng
        recyclerViewProductList = findViewById(R.id.recycler_view_productList);
        recyclerViewProductList.setLayoutManager(new LinearLayoutManager(this));

        productList = new ArrayList<>();
        productListAdapter = new ProductListAdapter(productList, this, 1);
        recyclerViewProductList.setAdapter(productListAdapter);

        if(orderId != null){
            settingAllProductsFromFirebase(orderId);
        }
    }

    //Xử lý load các sản phẩm thuộc đơn hàng
    private void settingAllProductsFromFirebase(String orderID) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("orders").child(orderID).child("items");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear(); // Clear the list before adding new data

                // Iterate over all items in the order
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null) {
                        productList.add(product); // Add each product to the list
                    }
                }
                productListAdapter.notifyDataSetChanged(); // Notify the adapter to refresh the UI
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewOdersActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatCreatedDate(String createdDateStr) {
        // Original date format
        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // Desired date format
        SimpleDateFormat targetFormat = new SimpleDateFormat("dd/MM/yyyy HH'h' mm 'phút'");
        Date date = null;
        try {
            // Parse the original date string
            date = originalFormat.parse(createdDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Format the date into the desired string
        return targetFormat.format(date);
    }

}
