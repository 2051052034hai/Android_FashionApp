package com.example.fashion_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.mindrot.jbcrypt.BCrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import Entities.User;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword, editTextUsername, editTextConfirmPassword;
    private Button registerButton;
    private DatabaseReference mDatabase;
    private TextView loginTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        // Initialize UI components
        initUI();

        // Set up Firebase authentication and database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Set up click listener for the register button
        registerButton.setOnClickListener(v -> registerUser());

        // Set up click listener for the login text view
        loginTextView.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void initUI() {
        loginTextView = findViewById(R.id.textViewLogin);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextUsername = findViewById(R.id.etUsername);
        registerButton = findViewById(R.id.btnRegister);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
    }

    //Xử lý đăng ký User
    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        int role = 1;

        // Validate inputs
        if (!validateInputs(email, password, username, confirmPassword)) return;
        // Hash the password
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Kiểm tra xem email đã tồn tại trong cơ sở dữ liệu hay chưa
        mDatabase.child("users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Email đã tồn tại
                    showToast("Địa chỉ email đã được sử dụng bởi một tài khoản khác.");
                } else {
                    // Email chưa tồn tại, lưu người dùng vào cơ sở dữ liệu
                    saveUser(email, hashedPassword, username, role);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                handleFirebaseError(databaseError.toException(), "Error checking email");
            }
        });
    }

    //Xử lý check bắt buộc nhập các cột
    private boolean validateInputs(String email, String password, String username, String confirmPassword) {
        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            showToast("Vui lòng nhập đầy đủ !!!");
            return false;
        }

        if (password.length() < 6) {
            showToast("Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showToast("Mật khẩu xác nhận không khớp");
            return false;
        }

        return true;
    }

    // Lưu thông tin User vào Firebase Realtime Database
    private void saveUser(String email, String password, String username, int role) {
        String userId = mDatabase.push().getKey();
        User userData = new User(userId, username, email, password, role);

        // Lưu dữ liệu người dùng vào bảng "users" trong Firebase Realtime Database
        mDatabase.child("users").child(userId).setValue(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Thông báo thành công và chuyển hướng tới LoginActivity
                        Toast.makeText(RegisterActivity.this, "Đăng kí thành công", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                    } else {
                        // Xử lý lỗi khi lưu dữ liệu
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(RegisterActivity.this, "Failed to save user data: " + errorMessage, Toast.LENGTH_LONG).show();
                        Log.e("RegisterActivity", "Failed to save user data", task.getException());
                    }
                });
    }

    //Hiển thị thông báo
    private void showToast(String message) {
        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    //Xử lý bắt lỗi
    private void handleFirebaseError(Exception exception, String logMessage) {
        String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
        showToast(logMessage + ": " + errorMessage);
        Log.e("RegisterActivity", logMessage, exception);
    }
}
