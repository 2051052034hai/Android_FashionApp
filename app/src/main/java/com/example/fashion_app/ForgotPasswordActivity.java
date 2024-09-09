package com.example.fashion_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextVerificationCode, editNewPassword;
    private Button buttonResetPassword, buttonSubmitCode, buttonNewPassword;
    private int sentVerificationCode;
    private ExecutorService executorService;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextVerificationCode = findViewById(R.id.editTextVerificationCode);
        buttonResetPassword = findViewById(R.id.buttonResetPassword);
        buttonSubmitCode = findViewById(R.id.buttonSubmitCode);
        editNewPassword = findViewById(R.id.editNewPassword);
        buttonNewPassword = findViewById(R.id.buttonNewPassword);

        // Initialize ExecutorService with a single thread
        executorService = Executors.newSingleThreadExecutor();

        buttonResetPassword.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Send email in the background using Executors
            sendResetEmail(email);
        });
    }

    private void sendResetEmail(String email) {
        // Tạo mã xác thực ngẫu nhiên 6 số
        int verificationCode = generateVerificationCode();

        executorService.execute(() -> {
            boolean result = sendEmail(email, verificationCode);
            // Use a Handler to post the result back to the UI thread
            new Handler(Looper.getMainLooper()).post(() -> {
                if (result) {
                    Toast.makeText(ForgotPasswordActivity.this, "Email sent successfully!", Toast.LENGTH_SHORT).show();
                    editTextEmail.setEnabled(false);
                    editTextVerificationCode.setVisibility(View.VISIBLE);
                    buttonSubmitCode.setVisibility(View.VISIBLE);
                } else {
                    editTextEmail.setEnabled(true);
                    Toast.makeText(ForgotPasswordActivity.this, "Failed to send email", Toast.LENGTH_SHORT).show();
                }
            });
        });

        buttonSubmitCode.setOnClickListener(v -> {
            String enteredCode = editTextVerificationCode.getText().toString().trim();

            if (enteredCode.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this, "Vui lòng nhập mã xác thực", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra mã xác thực
            if (Integer.parseInt(enteredCode) == sentVerificationCode) {
                Toast.makeText(ForgotPasswordActivity.this, "Mã xác thực chính xác!", Toast.LENGTH_SHORT).show();
                editTextVerificationCode.setEnabled(false);
                // Hiển thị input cho mật khẩu mới
                editNewPassword.setVisibility(View.VISIBLE);
                buttonNewPassword.setVisibility(View.VISIBLE);
            } else {
                editTextVerificationCode.setEnabled(true);
                Toast.makeText(ForgotPasswordActivity.this, "Mã xác thực không đúng", Toast.LENGTH_SHORT).show();
            }
        });

        buttonNewPassword.setOnClickListener(v -> {
            editNewPassword.setEnabled(false);
            String newPassword = editNewPassword.getText().toString().trim();

            if (newPassword.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
                return;
            }

            // Thực hiện cập nhật mật khẩu trong Firebase
            updatePasswordInFirebase(email, newPassword);
        });

    }

    private void updatePasswordInFirebase(String email, String newPassword) {
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Tìm userID dựa trên email
        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Lấy userID (giả sử userID là key của từng người dùng
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String userID = snapshot.getKey();  // Lấy userID
                        updatePasswordForUser(userID, newPassword);
                    }
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Email không tồn tại trong hệ thống", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi khi truy cập dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePasswordForUser(String userID, String newPassword) {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        Map<String, Object> userData = new HashMap<>();
        userData.put("passWord", hashedPassword);

        // Cập nhật mật khẩu dựa trên userID
        databaseReference.child(userID).updateChildren(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, "Cập nhật mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                        // Chuyển đến trang đăng nhập
                        Intent intent = new Intent(this, LoginActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Cập nhật mật khẩu thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean sendEmail(String recipientEmail, int verificationCode) {
        final String username = "vothanhtinh147@gmail.com";
        final String password = "abhtqfytwawyharq";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Reset Password");

            // Tạo nội dung email với HTML và CSS
            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 10px;'>"
                    + "<h2 style='color: #4CAF50; text-align: center;'>Reset Password</h2>"
                    + "<p>Xin chào,</p>"
                    + "<p>Bạn đã yêu cầu đặt lại mật khẩu. Đây là mã xác thực của bạn:</p>"
                    + "<div style='text-align: center; margin: 20px 0;'>"
                    + "<span style='display: inline-block; padding: 10px 20px; background-color: #f1f1f1; font-size: 24px; font-weight: bold; border-radius: 5px; border: 1px solid #ccc;'>"
                    + verificationCode
                    + "</span>"
                    + "</div>"
                    + "<p>Vui lòng nhập mã này để tiếp tục quá trình đặt lại mật khẩu.</p>"
                    + "<p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>"
                    + "<p>Trân trọng,<br>Đội ngũ hỗ trợ</p>"
                    + "</div>";

            // Gửi email dưới định dạng HTML
            message.setContent(htmlContent, "text/html; charset=utf-8");
            sentVerificationCode = verificationCode;
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int generateVerificationCode() {
        return (int) (Math.random() * 900000) + 100000;  // Tạo mã ngẫu nhiên từ 100000 đến 999999
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown the executor service to avoid leaks
        executorService.shutdown();
    }
}