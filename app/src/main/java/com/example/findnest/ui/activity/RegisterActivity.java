package com.example.findnest.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.findnest.R;
import com.example.findnest.api.IAuthenticationAPI;
import com.example.findnest.api.RetrofitClient;
import com.example.findnest.auth.AuthManager;
import com.example.findnest.model.request.authentication.RegisterModel;
import com.example.findnest.model.response.authentication.TokenModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameInput, emailInput, passwordInput, confirmPasswordInput, fullNameInput;
    private Button registerButton;
    private TextView loginText;

    private IAuthenticationAPI authService;
    private AuthManager authManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //khoi tao api service
        authManager = new AuthManager(RegisterActivity.this);
        authService = RetrofitClient.getClient(authManager).create(IAuthenticationAPI.class);

        usernameInput = findViewById(R.id.username_input);
        fullNameInput = findViewById(R.id.fullname_input);

        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input); // Ánh xạ trường mới
        registerButton = findViewById(R.id.register_button);
        loginText = findViewById(R.id.login_text);

        // Xử lý nút Đăng Ký
        registerButton.setOnClickListener(v -> {
            String fullName = fullNameInput.getText().toString().trim();
            String username = usernameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            // Kiểm tra dữ liệu
            if (username.isEmpty()
                    || email.isEmpty()
                    || password.isEmpty()
                    || confirmPassword.isEmpty()
                    || fullName.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 8) {
                Toast.makeText(this, "Mật khẩu cần có ít nhất 8 kí tự", Toast.LENGTH_SHORT).show();
            } else {
                // Logic đăng ký (gọi API ở đây nếu cần)
                RegisterModel model = new RegisterModel();
                model.setFullName(fullName);
                model.setUserName(username);
                model.setEmail(email);
                model.setPassword(password);

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                        .setTitle("Xác nhận")
                        .setMessage("Tạo tài khoản mới tại findnest ?")
                        .setPositiveButton("Xác nhận", (dialog, which) -> {
                            regiter(model);
                        })
                        .setNegativeButton("Hủy", (dialog, which) -> {
                            dialog.dismiss();
                        });
                AlertDialog dialog = builder.create();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                dialog.show();

            }
        });

        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(it);
            }
        });
    }

    private void regiter(RegisterModel request) {
        Log.d("Register", "Request:  " + new Gson().toJson(request));
        authService.register(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                    Intent it = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(it);
                } else {
                    Log.d("Register_Response", new Gson().toJson(response.body()));
                    Toast.makeText(RegisterActivity.this, response.code() + " - Đăng ký thất bại, Vui lòng thử lại với email khác", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Lỗi server, Vui lòng thử lại sau", Toast.LENGTH_SHORT).show();
            }
        });
    }
}