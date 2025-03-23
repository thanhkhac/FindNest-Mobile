package com.example.findnest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.findnest.R;
import com.example.findnest.api.IAuthenticationService;
import com.example.findnest.api.RetrofitClient;
import com.example.findnest.auth.AuthManager;
import com.example.findnest.model.authentication.LoginRequest;
import com.example.findnest.model.authentication.TokenModel;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput;
    private Button loginButton;
    private TextView registerText;

    private AuthManager authManager;
    private IAuthenticationService authService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //api
        authManager = new AuthManager(LoginActivity.this);
        authService = RetrofitClient.getClient(authManager).create(IAuthenticationService.class);


        // Ánh xạ các view
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        registerText = findViewById(R.id.register_text);

        // Xử lý nút Đăng Nhập
        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            } else {
                // Logic đăng nhập (gọi API ở đây nếu cần)
                LoginRequest request = new LoginRequest(username, password);
                login(request);
            }
        });

        // Chuyển sang màn hình Đăng Ký khi bấm vào text
        registerText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void login(LoginRequest request) {
        Log.d("Login", "Request:  " + new Gson().toJson(request));
        authService.login(request).enqueue(new Callback<TokenModel>() {
            @Override
            public void onResponse(Call<TokenModel> call, Response<TokenModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    authManager.saveTokens(response.body().getAccessToken(), response.body().getRefreshToken());
                    //Log.d("Shared_Pref", new Gson().toJson(authManager.getAccessToken() + authManager.getRefreshToken()));
                    Toast.makeText(LoginActivity.this, "Login successfull", Toast.LENGTH_SHORT).show();

                    Intent it = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(it);

                } else {
                    Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TokenModel> call, Throwable t) {
                Log.e("Login", "Error: " + t.getMessage());
            }
        });
    }
}