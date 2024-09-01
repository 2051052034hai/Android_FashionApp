package com.example.fashion_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewParent;
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

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import Common.DrawerLayoutActivity;
import Entities.User;

public class AddUserActivity extends DrawerLayoutActivity {
    private TextInputEditText userName, userEmail, userPassword, userNewPassword;
    private DatabaseReference databaseReference;
    private Button btnSubmit, btnChangePassword;
    private String userID;
    private Spinner spinnerUserRole;
    private int selectedChangePass = 0;
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
        btnChangePassword = findViewById(R.id.btnChangePassword);
        userNewPassword = findViewById(R.id.newPassword);
        userNewPassword.setVisibility(View.GONE);

        // Kiểm tra nếu là trường hợp cập nhật
        Intent intent = getIntent();
        if (intent.hasExtra("USER_ID") && intent.getStringExtra("USER_ID") != null) {
            userID = intent.getStringExtra("USER_ID");
            loadUserData(userID);
            userPassword.setEnabled(false);
            btnSubmit.setText("Cập nhật");
        }
        else {
            userPassword.setEnabled(true);
            btnChangePassword.setVisibility(View.GONE);
        }

        btnChangePassword.setOnClickListener(v -> {
            if (userNewPassword.getVisibility() == View.VISIBLE) {
                btnChangePassword.setText("Thay đổi");
                userNewPassword.setVisibility(View.GONE);
                ViewParent parent = userNewPassword.getParent().getParent();
                if (parent instanceof View) {
                    View parentView = (View) parent;
                    // Set the parent view's visibility to VISIBLE
                    parentView.setVisibility(View.GONE);

                    // If the parent view is a TextInputLayout, clear the error
                    if (parentView instanceof TextInputLayout) {
                        ((TextInputLayout) parentView).setError(null);
                    }
                }
                selectedChangePass = 0;
            } else {
                btnChangePassword.setText("Đóng");
                ViewParent parent = userNewPassword.getParent().getParent();
                if (parent instanceof View) {
                    View parentView = (View) parent;
                    // Set the parent view's visibility to VISIBLE
                    parentView.setVisibility(View.VISIBLE);
                }
                userNewPassword.setVisibility(View.VISIBLE);
                selectedChangePass = 1;
            }
        });

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
                        userPassword.setText(user.getPassWord());
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
        String userpassword = Objects.requireNonNull(userPassword.getText()).toString();
        String hashedPassword = BCrypt.hashpw(userpassword, BCrypt.gensalt());
        // Kiểm tra nếu productCatId đã tồn tại (chế độ cập nhật) hoặc không (chế độ thêm mới)
        String userId = this.userID != null ? this.userID : databaseReference.push().getKey();

        // Tạo một map để chứa dữ liệu
        Map<String, Object> userData = new HashMap<>();

        // Nếu là trường hợp thêm mới, thêm id vào dữ liệu
        if (this.userID == null) {
            userData.put("id", userId);
            userData.put("passWord", hashedPassword);
        }

        String txtNewPassword = userNewPassword.getText().toString();
        if( txtNewPassword != null && !txtNewPassword.isEmpty() && selectedChangePass == 1 ){
            String hashedNewPassword = BCrypt.hashpw(txtNewPassword, BCrypt.gensalt());
            userData.put("passWord", hashedNewPassword);
        }

        userData.put("userName", username);
        userData.put("email", useremail);
        userData.put("role", selectedRoleID);

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
        userPassword.setText("");
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

        if (Objects.requireNonNull(userPassword.getText()).toString().trim().isEmpty()) {
            ((TextInputLayout) userPassword.getParent().getParent()).setError("Bạn phải nhập vào mật khẩu");
            isValid = false;
        } else {
            ((TextInputLayout) userPassword.getParent().getParent()).setError(null);
        }

        if(selectedChangePass == 1){
            if (Objects.requireNonNull(userNewPassword.getText()).toString().trim().isEmpty()) {
                ((TextInputLayout) userNewPassword.getParent().getParent()).setError("Bạn phải nhập vào mật khẩu mới");
                isValid = false;
            } else {
                ((TextInputLayout) userNewPassword.getParent().getParent()).setError(null);
            }
        }
        return isValid;
    }
}
