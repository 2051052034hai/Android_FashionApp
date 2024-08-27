package com.example.fashion_app;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Entities.Orders;

public class ViewOdersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_orders_view);

        // Lấy ID sản phẩm từ Intent
        String orderId = getIntent().getStringExtra("ORDER_ID");
        String username = getIntent().getStringExtra("USERNAME");

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
                    userName.setText(username);
                    totalAmount.setText(String.valueOf(orders.getTotalAmount()));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi
                Log.e("ViewOdersActivity", "Error loading orders", databaseError.toException());
            }
        });
    }
}
