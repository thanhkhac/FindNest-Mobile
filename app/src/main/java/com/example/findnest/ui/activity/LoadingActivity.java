package com.example.findnest.ui.activity;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.findnest.R;
import com.example.findnest.api.IAuthenticationAPI;
import com.example.findnest.api.client.retrofit.RetrofitClient;
import com.example.findnest.api.client.auth.AuthManager;
import com.example.findnest.model.response.authentication.TokenModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoadingActivity extends AppCompatActivity {

    private AuthManager authManager;
    private IAuthenticationAPI authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading);
        try {
            Animate();
            //refresh token - auto login

            //api
            authManager = new AuthManager(LoadingActivity.this);
            authService = RetrofitClient.getClient(authManager).create(IAuthenticationAPI.class);

            checkToken();
        } catch (Exception ex) {
            Log.e(TAG, "Error in onCreate: " + ex.getMessage(), ex);
            Toast.makeText(LoadingActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    void checkToken() {
        //get token in shared preferences
        String accessToken = authManager.getAccessToken();
        String refreshToken = authManager.getRefreshToken();

        if ( accessToken == null || refreshToken == null || accessToken.isEmpty() || refreshToken.isEmpty()) {
            Intent it = new Intent(LoadingActivity.this, LoginActivity.class);
            startActivity(it);
            finish();
        }

        TokenModel tokenModel = new TokenModel();
        tokenModel.setAccessToken(accessToken);
        tokenModel.setRefreshToken(refreshToken);

        try {
            authService.refreshToken(tokenModel).enqueue(new Callback<TokenModel>() {
                @Override
                public void onResponse(Call<TokenModel> call, Response<TokenModel> response) {
//                    Log.d("API_REQUEST", "URL: " + call.request().url());
//                    Log.d("API_REQUEST", "Method: " + call.request().method());
//                    Log.d("API_REQUEST", "Headers: " + call.request().headers().toString());
//                    Log.d("API_REQUEST", "Body: " + new Gson().toJson(tokenModel));
//                    Log.d("API_REQUEST", "Response: " + response.code());
//                    Log.d("API_REQUEST", "Response: " + response.isSuccessful());
                    if (response.isSuccessful()) {
                        TokenModel tokenResponse = response.body();
                        if (tokenResponse != null) {
//                            Log.d("API_RESPONSE", "AccessToken: " + tokenResponse.getAccessToken());
//                            Log.d("API_RESPONSE", "RefreshToken: " + tokenResponse.getRefreshToken());
                            authManager.saveTokens(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());

                            Intent it = new Intent(LoadingActivity.this, MainActivity.class);
                            startActivity(it);
                        } else {
                            Log.e("API_RESPONSE", "RefreshToken body is null!");
                        }
                    } else {
                        Intent it = new Intent(LoadingActivity.this, LoginActivity.class);
                        startActivity(it);
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<TokenModel> call, Throwable t) {
                    Intent it = new Intent(LoadingActivity.this, LoginActivity.class);
                    startActivity(it);
                    finish();
                }
            });
        } catch (Exception ex) {
            Toast.makeText(LoadingActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    void Animate() {
        // Find Views
        TextView title = findViewById(R.id.title);
        TextView subTitle = findViewById(R.id.sub_title);

        // Initially hide views
        title.setAlpha(0f);
        subTitle.setAlpha(0f);

        // Title Animation: Fade In + Slide Down
        title.animate()
                .alpha(1f)
                .translationYBy(50f) // Move down slightly
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(800)
                .start();

        // Subtitle Animation: Fade In + Slide Up
        subTitle.animate()
                .alpha(1f)
                .translationYBy(-50f) // Move up slightly
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setStartDelay(500) // Delay to make it look smooth
                .setDuration(900)
                .start();
    }
}