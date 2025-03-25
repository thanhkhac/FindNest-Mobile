package com.example.findnest.ui.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.example.findnest.api.IUserAPI;
import com.example.findnest.api.client.auth.AuthManager;
import com.example.findnest.api.client.retrofit.RetrofitClient;
import com.example.findnest.model.request.user.ForgotPasswordReq;
import com.example.findnest.model.request.user.ResetPasswordReq;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {


    private TextView clock, register_text, login_text;
    private Button btn_reset_password;

    private EditText input_confirmation_password, input_password, input_code, input_email;

    private AuthManager authManager;
    private IUserAPI userAPI;
    private final String PUT_EXTRA_EMAIL = "email";
    private String email;

    void Init() {
        authManager = new AuthManager(ResetPasswordActivity.this);
        userAPI = RetrofitClient.getClient(authManager).create(IUserAPI.class);

        clock = findViewById(R.id.clock);
        register_text = findViewById(R.id.register_text);
        login_text = findViewById(R.id.login_text);
        btn_reset_password = findViewById(R.id.btn_reset_password);
        input_confirmation_password = findViewById(R.id.input_confirmation_password);
        input_password = findViewById(R.id.input_password);
        input_code = findViewById(R.id.input_code);
        input_email = findViewById(R.id.input_email);

        Intent it = getIntent();
        email = it.getStringExtra(PUT_EXTRA_EMAIL);
        input_email.setText(email);
    }

    private CountDownTimer timer;
    private static final long START_TIME_IN_MILLIS = 30 * 60 * 1000;

    private void startTimer() {
        timer = new CountDownTimer(START_TIME_IN_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                clock.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                clock.setText("00:00");
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel(); // Cancel the timer to prevent memory leaks
        }
    }

    void InitEvents() {
        // Chuyển sang màn hình Đăng Nhập khi bấm vào text
        login_text.setOnClickListener(v -> {
            new AlertDialog.Builder(ResetPasswordActivity.this)
                    .setTitle("Thông báo")
                    .setMessage("Thao tác này có thể hủy tiến trình hiện tại.")
                    .setPositiveButton("Xác nhận", (dialog, which) -> {

                        Intent it = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                        startActivity(it);
                        finish();

                    }).setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss()).show();
        });

        // Chuyển sang màn hình Đăng Ký khi bấm vào text
        register_text.setOnClickListener(v -> {
            new AlertDialog.Builder(ResetPasswordActivity.this)
                    .setTitle("Thông báo")
                    .setMessage("Thao tác này có thể hủy tiến trình hiện tại.")
                    .setPositiveButton("Xác nhận", (dialog, which) -> {

                        Intent intent = new Intent(ResetPasswordActivity.this, RegisterActivity.class);
                        startActivity(intent);
                        finish();

                    }).setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss()).show();
        });

        btn_reset_password.setOnClickListener(v -> {
            String code = input_code.getText().toString().trim();
            String password = input_password.getText().toString().trim();
            String confirmPassword = input_confirmation_password.getText().toString().trim();

            if (code.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(ResetPasswordActivity.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            } else if (!confirmPassword.equals(password)) {
                Toast.makeText(ResetPasswordActivity.this, "Nhập lại mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 8 || password.length() > 100) {
                Toast.makeText(ResetPasswordActivity.this, "Mật khẩu mới cần nằm trong khoảng 8 - 100 kí tự", Toast.LENGTH_SHORT).show();
            } else {
                new AlertDialog.Builder(ResetPasswordActivity.this)
                        .setTitle("Xác nhận")
                        .setMessage("Xác nhận đặt lại mật khẩu ?")
                        .setPositiveButton("Xác nhận", (dialog, which) -> {

                            ResetPasswordReq model = new ResetPasswordReq();
                            model.setEmail(email);
                            model.setResetCode(code);
                            model.setNewPassword(password);

                            ResetPassword(model);

                        }).setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss()).show();
            }
        });
    }

    void ResetPassword(ResetPasswordReq request) {
        userAPI.resetPassword(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ResetPasswordActivity.this, "Đặt lại mật khẩu thành công", Toast.LENGTH_SHORT).show();
                    Intent it = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    startActivity(it);
                } else {
                    Toast.makeText(ResetPasswordActivity.this, " Đặt lại mật khẩu thất bại, vui lòng kiểm tra lại thông tin", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ResetPasswordActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);
        Init();
        InitEvents();
        startTimer();
    }
}