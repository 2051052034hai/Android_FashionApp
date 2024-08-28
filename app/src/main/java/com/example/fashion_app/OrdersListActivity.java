package com.example.fashion_app;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import Adapter.OrdersListAdapter;
import Common.DrawerLayoutActivity;
import Entities.Orders;
import Entities.Product;
import Entities.User;

public class OrdersListActivity extends DrawerLayoutActivity {

    private RecyclerView recyclerViewOrdersList;
    private OrdersListAdapter ordersListAdapter;
    private List<Orders> ordersList;

    private  String userName;
    private SearchView search_view;

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

        // Xử lý khi click vào nút search sản phẩm
        search_view = findViewById(R.id.search_view);
        search_view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    // If the search view is cleared, show all products
                    settingAllOrdersFromFirebase();
                } else {
                    // Filter product list based on search text
                   filterOrdersList(newText);
                }
                return true;
            }
        });
    }

    //Hàm xử lý lọc đơn hàng theo tên khách hàng
    private void filterOrdersList(String query) {
        List<Orders> filteredList = new ArrayList<>();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Normalize query to ignore case and accents
        String normalizedQuery = normalizeString(query.toLowerCase());

        // Fetch all users data at once
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Create a map to store user IDs and user names
                Map<String, String> userMap = new HashMap<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        userMap.put(user.getId(), normalizeString(user.getUserName().toLowerCase()));
                    }
                }

                // Now filter the ordersList based on the user data fetched
                for (Orders order : ordersList) {
                    String userName = userMap.get(order.getUserId());
                    if (userName != null && userName.contains(normalizedQuery)) {
                        filteredList.add(order);
                    }
                }

                ordersListAdapter.updateList(filteredList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }


    // Hàm để loại bỏ dấu tiếng Việt
    private String normalizeString(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
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
