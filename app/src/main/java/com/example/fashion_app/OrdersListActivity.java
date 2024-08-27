package com.example.fashion_app;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Adapter.OrdersListAdapter;
import Common.DrawerLayoutActivity;
import Entities.Orders;

public class OrdersListActivity extends DrawerLayoutActivity {

    private RecyclerView recyclerViewOrdersList;
    private OrdersListAdapter ordersListAdapter;
    private List<Orders> ordersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_orders_management, findViewById(R.id.content_frame));

        recyclerViewOrdersList = findViewById(R.id.recycler_view_ordersList);
        recyclerViewOrdersList.setLayoutManager(new LinearLayoutManager(this));

        ordersList = new ArrayList<>();
        ordersListAdapter = new OrdersListAdapter(ordersList, this);
        recyclerViewOrdersList.setAdapter(ordersListAdapter);

        //Load danh sách đơn hàng
        settingAllOrdersFromFirebase();
    }

    //Xử lý load thông tin cho danh muc đơn hàng
    private void settingAllOrdersFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("orders");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ordersList.clear();
                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    Orders orders = orderSnapshot.getValue(Orders.class);
                    if (orders != null) {
                        ordersList.add(orders);
                    }
                }
                ordersListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrdersListActivity.this, "Failed to load Orders", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
