package com.example.findnest.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.findnest.R;
import com.example.findnest.api.IAuthenticationAPI;
import com.example.findnest.api.client.retrofit.RetrofitClient;
import com.example.findnest.api.client.auth.AuthManager;
import com.example.findnest.model.ErrorResponse;
import com.example.findnest.model.request.authentication.LoginRequest;
import com.example.findnest.model.response.authentication.TokenModel;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput;
    private Button loginButton;
    private TextView registerText, forgot_password_text;

    private AuthManager authManager;
    private IAuthenticationAPI authService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //api
        authManager = new AuthManager(LoginActivity.this);
        authService = RetrofitClient.getClient(authManager).create(IAuthenticationAPI.class);

        // Ánh xạ các view
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        registerText = findViewById(R.id.register_text);
        forgot_password_text = findViewById(R.id.forgot_password_text);


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

        forgot_password_text.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });


    }

    private void login(LoginRequest request) {
        Log.d("Login", "Request: " + new Gson().toJson(request));
        authService.login(request).enqueue(new Callback<TokenModel>() {
            @Override
            public void onResponse(Call<TokenModel> call, Response<TokenModel> response) {
                Log.d("LoginResponseCode", "Response Code: " + response.code()); // Log mã trạng thái
                if (response.isSuccessful() && response.body() != null) {
                    authManager.saveTokens(response.body().getAccessToken(), response.body().getRefreshToken());
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                    Intent it = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(it);
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.d("LoginErrorBody", "Error Body: " + errorBody);

                            Gson gson = new Gson();
                            ErrorResponse errorResponse = gson.fromJson(errorBody, ErrorResponse.class);
                            Log.d("LoginErrorResponse", "Parsed Error: " + new Gson().toJson(errorResponse));

                            if (errorResponse.getErrors() != null) {
                                Map<String, List<String>> errors = errorResponse.getErrors();
                                if (errors.containsKey("unauthorized")) {
                                    Toast.makeText(LoginActivity.this, "Sai thông tin đăng nhập", Toast.LENGTH_SHORT).show();
                                } else {
                                    String message = errorResponse.getMessage() != null ? errorResponse.getMessage() : "Đăng nhập thất bại";
                                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                String message = errorResponse.getMessage() != null ? errorResponse.getMessage() : "Đăng nhập thất bại";
                                Toast.makeText(LoginActivity.this, response.code() + " - " + message, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d("LoginErrorBody", "Error Body is null");
                            Toast.makeText(LoginActivity.this, response.code() + " - Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("LoginError", "IOException: " + e.getMessage());
                        Toast.makeText(LoginActivity.this, "Lỗi xử lý phản hồi từ server", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("LoginError", "Exception: " + e.getMessage());
                        Toast.makeText(LoginActivity.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<TokenModel> call, Throwable t) {
                Log.e("LoginInFo", "Error: " + t.getMessage(), t); // In stack trace
//                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Toast.makeText(LoginActivity.this, "Sai thông tin đăng nhập", Toast.LENGTH_SHORT).show();
            }
        });
    }
}