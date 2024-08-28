package com.example.fashion_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
import java.util.List;
import java.util.regex.Pattern;

import Adapter.UsersListAdapter;
import Common.DrawerLayoutActivity;
import Entities.User;

public class UserListActivity extends DrawerLayoutActivity {

    private RecyclerView recyclerViewUserList;
    private UsersListAdapter usersListAdapter;
    private ImageView btnAddNew;
    private DatabaseReference databaseReference;
    private List<User> usersList;
    private SearchView search_view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_users_management, findViewById(R.id.content_frame));

        recyclerViewUserList = findViewById(R.id.recycler_view_userList);
        recyclerViewUserList.setLayoutManager(new LinearLayoutManager(this));

        usersList = new ArrayList<>();
        usersListAdapter = new UsersListAdapter(usersList, this);
        recyclerViewUserList.setAdapter(usersListAdapter);

        //Load danh sách người dùng
        settingAllUsersFromFirebase();

        // Xử lý khi click vào button thêm mới
        btnAddNew = findViewById(R.id.btn_addnew_item);
        btnAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserListActivity.this, AddUserActivity.class);
                startActivity(intent);
            }
        });

        // Xử lý khi click vào nút search loại sản phẩm
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
                    settingAllUsersFromFirebase();
                } else {
                    // Filter product list based on search text
                    filterUsersList(newText);
                }
                return true;
            }
        });

    }

    //Hàm xử lý lọc loại sản phẩm theo tên
    private void filterUsersList(String query) {
        List<User> filteredList = new ArrayList<>();

        // Normalize query để không phân biệt dấu và chữ hoa thường
        String normalizedQuery = normalizeString(query.toLowerCase());

        for (User user : usersList) {
            // Normalize tên sản phẩm để so sánh
            String normalizedUserName = normalizeString(user.getUserName().toLowerCase());

            if (normalizedUserName.contains(normalizedQuery)) {
                filteredList.add(user);
            }
        }
        usersListAdapter.updateList(filteredList);
    }

    // Hàm để loại bỏ dấu tiếng Việt
    private String normalizeString(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }

    //Xử lý load thông tin cho danh muc người dùng
    private void settingAllUsersFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        usersList.add(user);
                    }
                }
                usersListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserListActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
