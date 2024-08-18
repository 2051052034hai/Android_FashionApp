package com.example.fashion_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;  // Nhập lớp View
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

public class LoginActivity extends AppCompatActivity {
    private EditText editText_User;
    private EditText editText_Password;
    private Button saveButton;
    private TextView forgotPasswordTextView;
    private TextView registerTextView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Liên kết các thành phần giao diện
        editText_User = findViewById(R.id.textViewEmail);
        editText_Password = findViewById(R.id.textViewPassword);
        saveButton = findViewById(R.id.btnLogin);
        forgotPasswordTextView = findViewById(R.id.textViewForgotPassword);
        registerTextView = findViewById(R.id.textViewRegister);

        // Khởi tạo Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        saveButton.setOnClickListener(v -> {
            // Lấy dữ liệu từ EditText
            String email = editText_User.getText().toString().trim();
            String password = editText_Password.getText().toString().trim();

            // Kiểm tra nếu trường không trống
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra độ dài mật khẩu
            if (password.length() < 6) {
                Toast.makeText(LoginActivity.this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            // Đăng nhập với Firebase Authentication
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish(); // Đóng LoginActivity để không trở lại màn hình đăng nhập
                        } else {
                            Exception exception = task.getException();
                            if (exception instanceof FirebaseAuthInvalidUserException) {
                                // Người dùng không tồn tại
                                Toast.makeText(LoginActivity.this, "Tài khoản không tồn tại", Toast.LENGTH_LONG).show();
                            } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                // Mật khẩu không chính xác
                                Toast.makeText(LoginActivity.this, "Mật khẩu không đúng", Toast.LENGTH_LONG).show();
                            } else {
                                // Các lỗi khác
                                Toast.makeText(LoginActivity.this, "Authentication failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });

        // Thiết lập sự kiện nhấp vào TextView
        forgotPasswordTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Thiết lập sự kiện nhấp vào TextView Sign Up
        registerTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

}
