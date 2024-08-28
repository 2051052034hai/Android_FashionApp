package com.example.fashion_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import Common.DrawerLayoutActivity;
import Entities.User;

public class AddUserActivity extends DrawerLayoutActivity {
    private TextInputEditText userName, userEmail, userPassword;
    private DatabaseReference databaseReference;
    private Button btnSubmit;
    private String userID;
    private Spinner spinnerUserRole;
    private  int selectedRoleID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_add_user, findViewById(R.id.content_frame));

        // Initialize Firebase references
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Initialize views
        userName = findViewById(R.id.Username);
        userEmail = findViewById(R.id.Email);
        userPassword = findViewById(R.id.Password);
        spinnerUserRole = findViewById(R.id.spinnerRole);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Kiểm tra nếu là trường hợp cập nhật
        Intent intent = getIntent();
        if (intent.hasExtra("USER_ID") && intent.getStringExtra("USER_ID") != null) {
            userID = intent.getStringExtra("USER_ID");
            loadUserData(userID);
            btnSubmit.setText("Cập nhật");
        }

        //Xử lý khi click vào button thêm mới sản phẩm
        btnSubmit.setOnClickListener(v -> {
            if (validateInputs()) {
                saveUserData();
            }
        });


        List<String> userNames = Arrays.asList("Khách hàng", "Quản trị");

        // Tạo Adapter và nạp dữ liệu vào Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(AddUserActivity.this, android.R.layout.simple_spinner_item, userNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUserRole.setAdapter(adapter);

        // Thiết lập sự kiện chọn cho Spinner
        spinnerUserRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Lưu ID của người dùng được chọn
                selectedRoleID = position == 0 ? 1 : 2;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Xử lý khi không có lựa chọn nào được chọn
                selectedRoleID = -1;
            }
        });
    }

    //Hàm xử lý load dữ liệu người dùng khi cập nhật
    private void loadUserData(String userID) {
        DatabaseReference userRef = databaseReference.child(userID);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        userName.setText(user.getUserName());
                        userEmail.setText(user.getEmail());
                    }
                } else {
                    Toast.makeText(AddUserActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AddUserActivity.this, "Failed to load User data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Hàm xử lý tạo mới/ cập nhật người dùng
    private void saveUserData() {
        String username = Objects.requireNonNull(userName.getText()).toString();
        String useremail = Objects.requireNonNull(userEmail.getText()).toString();

        // Kiểm tra nếu productCatId đã tồn tại (chế độ cập nhật) hoặc không (chế độ thêm mới)
        String userId = this.userID != null ? this.userID : databaseReference.push().getKey();

        // Tạo một map để chứa dữ liệu
        Map<String, Object> userData = new HashMap<>();

        // Nếu là trường hợp thêm mới, thêm id vào dữ liệu
        if (this.userID == null) {
            userData.put("id", userId);
        }

        userData.put("userName", username);
        userData.put("email", useremail);

        // Chọn phương thức lưu trữ phù hợp (cập nhật hoặc thêm mới)
        Task<Void> saveTask = this.userID != null ?
                databaseReference.child(userId).updateChildren(userData) :
                databaseReference.child(userId).setValue(userData);

        saveTask.addOnCompleteListener(task -> {
            String message = task.isSuccessful() ?
                    (this.userID != null ? "Cập nhật người dùng thành công" : "Thêm mới người dùng thành công") :
                    (this.userID != null ? "Failed to update user" : "Failed to save user");

            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                    .setAction(task.isSuccessful() ? "OK" : "RETRY", v -> {
                    })
                    .show();

            if (task.isSuccessful() && this.userID == null) {
                resetForm();
            }
        });
    }


    //Hàm xử lý reset form sau khi thêm mới loại sản phẩm
    private void resetForm() {
        userName.setText("");
        userEmail.setText("");
        userID = null;
        btnSubmit.setText("Thêm người dùng");
    }

    //Hàm xử lý check bắt buộc nhập cho các trường dữ liệu
    private boolean validateInputs() {
        boolean isValid = true;

        if (Objects.requireNonNull(userName.getText()).toString().trim().isEmpty()) {
            ((TextInputLayout) userName.getParent().getParent()).setError("Bạn phải nhập vào tên người dùng");
            isValid = false;
        } else {
            ((TextInputLayout) userName.getParent().getParent()).setError(null);
        }

        if (Objects.requireNonNull(userEmail.getText()).toString().trim().isEmpty()) {
            ((TextInputLayout) userEmail.getParent().getParent()).setError("Bạn phải nhập vào email");
            isValid = false;
        } else {
            ((TextInputLayout) userEmail.getParent().getParent()).setError(null);
        }

        return isValid;
    }
}
