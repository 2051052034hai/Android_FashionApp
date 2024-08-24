package com.example.fashion_app;

import android.content.Intent;
import android.os.Bundle;
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

public class LoginActivity extends AppCompatActivity {
    private EditText editText_User;
    private EditText editText_Password;
    private Button saveButton;
    private TextView forgotPasswordTextView;
    private TextView registerTextView;
    private DatabaseReference usersRef;

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

        // Khởi tạo Firebase Realtime Database
        usersRef = FirebaseDatabase.getInstance().getReference("users");

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

            // Kiểm tra thông tin đăng nhập với cơ sở dữ liệu
            usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String storedPasswordHash  = userSnapshot.child("passWord").getValue(String.class);

                            if (BCrypt.checkpw(password, storedPasswordHash)) {

                                String userID = userSnapshot.getKey();
                                String username = userSnapshot.child("username").getValue(String.class);
                                int role = userSnapshot.child("role").getValue(int.class);

                                User session = User.getInstance();
                                session.setEmail(email);
                                session.setUserName(username);
                                session.setId(userID);
                                session.setRole(role);

                                Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish(); // Đóng LoginActivity để không trở lại màn hình đăng nhập
                                return;
                            } else {
                                Toast.makeText(LoginActivity.this, "Mật khẩu không đúng", Toast.LENGTH_LONG).show();
                            }
                        }
                        Toast.makeText(LoginActivity.this, "Mật khẩu không đúng", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Tài khoản không tồn tại", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(LoginActivity.this, "Đã xảy ra lỗi: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
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
