package com.example.findnest.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.findnest.R;
import com.example.findnest.api.ITestAPI;
import com.example.findnest.api.client.retrofit.RetrofitClient;
import com.example.findnest.api.client.auth.AuthManager;
import com.example.findnest.model.response.plan.PlanDetailRes;
import com.example.findnest.model.response.authentication.TokenModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {


    Button btn;

    private ITestAPI testService;
    private AuthManager authManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        btn = findViewById(R.id.btn);

        authManager = new AuthManager(HomeActivity.this);

        testService = RetrofitClient.getClient(authManager).create(ITestAPI.class);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TokenModel token = new TokenModel();

                token.setAccessToken(authManager.getAccessToken());
                token.setRefreshToken(authManager.getRefreshToken());

                testService.getPlan(token).enqueue(new Callback<List<PlanDetailRes>>() {
                    @Override
                    public void onResponse(Call<List<PlanDetailRes>> call, Response<List<PlanDetailRes>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(HomeActivity.this, String.valueOf(response.body().size()), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(HomeActivity.this, "GetPlan Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<PlanDetailRes>> call, Throwable t) {
                        Toast.makeText(HomeActivity.this, "GetPlan Failed!", Toast.LENGTH_SHORT).show();
                        Log.e("GetPlanERROR", "GetPlan Failed!");
                    }
                });

            }
        });

    }
}