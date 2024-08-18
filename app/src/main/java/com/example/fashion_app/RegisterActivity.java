package com.example.fashion_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import Entities.User;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword, editTextUsername;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView loginTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        // Liên kết các thành phần giao diện
        loginTextView = findViewById(R.id.textViewLogin);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextUsername = findViewById(R.id.etUsername);  // Thêm trường tên người dùng
        registerButton = findViewById(R.id.btnRegister);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        registerButton.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String username = editTextUsername.getText().toString().trim();  // Lấy giá trị tên người dùng

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Vui lòng nhập đầy đủ !!!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra độ dài mật khẩu
            if (password.length() < 6) {
                Toast.makeText(RegisterActivity.this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra xem email đã tồn tại hay chưa
            mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    boolean emailExists = !task.getResult().getSignInMethods().isEmpty();
                    if (emailExists) {
                        Toast.makeText(RegisterActivity.this, "Địa chỉ email đã được sử dụng bởi một tài khoản khác.", Toast.LENGTH_LONG).show();
                    } else {
                        // Đăng ký người dùng với Firebase Authentication
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this, task1 -> {
                                    if (task1.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if (user != null) {
                                            // Lưu thông tin người dùng vào Realtime Database
                                            User userData = new User(username, email);
                                            mDatabase.child("users").child(user.getUid()).setValue(userData)
                                                    .addOnCompleteListener(task2 -> {
                                                        if (task2.isSuccessful()) {
                                                            Toast.makeText(RegisterActivity.this, "Đăng kí thành công", Toast.LENGTH_SHORT).show();
                                                            //Chuyển hướng hoặc thực hiện hành động khác
                                                             Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                             startActivity(intent);
                                                            // finish();
                                                        } else {
                                                            String errorMessage = task2.getException() != null ? task2.getException().getMessage() : "Unknown error";
                                                            Toast.makeText(RegisterActivity.this, "Failed to save user data: " + errorMessage, Toast.LENGTH_LONG).show();
                                                            Log.e("RegisterActivity", "Failed to save user data", task2.getException());
                                                        }
                                                    });
                                        }
                                    } else {
                                        if (task1.getException() instanceof FirebaseAuthUserCollisionException) {
                                            // Địa chỉ email đã được sử dụng
                                            Toast.makeText(RegisterActivity.this, "Địa chỉ email đã được sử dụng bởi một tài khoản khác.", Toast.LENGTH_LONG).show();
                                        } else {
                                            // Các lỗi khác
                                            String errorMessage = task1.getException() != null ? task1.getException().getMessage() : "Unknown error";
                                            Toast.makeText(RegisterActivity.this, "Registration failed: " + errorMessage, Toast.LENGTH_LONG).show();
                                        }
                                        Log.e("RegisterActivity", "Registration failed", task1.getException());
                                    }
                                });
                    }
                } else {
                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                    Toast.makeText(RegisterActivity.this, "Error checking email: " + errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("RegisterActivity", "Error checking email", task.getException());
                }
            });
        });
        // Thiết lập sự kiện nhấp vào TextView Sign Up
        loginTextView.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }


}
