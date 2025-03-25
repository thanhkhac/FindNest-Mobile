package com.example.findnest.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.findnest.R;
import com.example.findnest.api.IAuthenticationAPI;
import com.example.findnest.api.IUserAPI;
import com.example.findnest.api.client.auth.AuthManager;
import com.example.findnest.api.client.retrofit.RetrofitClient;
import com.example.findnest.model.request.user.ForgotPasswordReq;
import com.example.findnest.model.request.user.UserContactInfoReq;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText input_email;
    private TextView registerText, loginText;
    private Button btn_send_email;
    private AuthManager authManager;
    private IUserAPI userAPI;

    private final String PUT_EXTRA_EMAIL = "email";

    void Init() {
        authManager = new AuthManager(ForgotPasswordActivity.this);
        userAPI = RetrofitClient.getClient(authManager).create(IUserAPI.class);

        input_email = findViewById(R.id.input_email);
        btn_send_email = findViewById(R.id.btn_send_email);
        loginText = findViewById(R.id.login_text);
        registerText = findViewById(R.id.register_text);
    }

    void InitEvent() {

        // Chuyển sang màn hình Đăng Nhập khi bấm vào text
        loginText.setOnClickListener(v -> {
            Intent it = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(it);
            finish();
        });

        // Chuyển sang màn hình Đăng Ký khi bấm vào text
        registerText.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        btn_send_email.setOnClickListener(v -> {
            String email = input_email.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this, "Vui lòng điền email để tiếp tục", Toast.LENGTH_SHORT).show();
            } else {
                new AlertDialog.Builder(ForgotPasswordActivity.this)
                        .setTitle("Xác nhận")
                        .setMessage("Gửi mã xác nhận đặt lại mật khẩu đến địa chỉ email: " + email + " ?")
                        .setPositiveButton("Xác nhận", (dialog, which) -> {

                            ForgotPasswordReq model = new ForgotPasswordReq();
                            model.setEmail(email);
                            sendEmail(model);

                        }).setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss()).show();
            }
        });
    }

    void sendEmail(ForgotPasswordReq request) {
        userAPI.sendEmailForgotPassword(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Mã đã được gửi đi.", Toast.LENGTH_SHORT).show();
                    Intent it = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                    it.putExtra(PUT_EXTRA_EMAIL, request.getEmail());
                    startActivity(it);
                    finish();

                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Lỗi, vui lòng kiểm tra lại email", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi: " + t.getMessage() + ", vui lòng kiểm tra lại email", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        Init();
        InitEvent();
    }
}