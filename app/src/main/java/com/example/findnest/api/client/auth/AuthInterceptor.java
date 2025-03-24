package com.example.findnest.api.client.auth;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.findnest.api.IAuthenticationAPI;
import com.example.findnest.model.response.authentication.TokenModel;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AuthInterceptor implements Interceptor {


    private static final String BASE_URL = "https://thanhkhac.id.vn/";
//    private final String BASE_URL = "http://192.168.0.101:8080/";
//    private static final String BASE_URL = "https://10.0.2.2:7011/";

    private AuthManager _authManager;
    private IAuthenticationAPI _authenticationService;

    public AuthInterceptor(AuthManager authManager) {
        this._authManager = authManager;

        OkHttpClient client = new OkHttpClient.Builder().build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        _authenticationService = retrofit.create(IAuthenticationAPI.class);
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String accessToken = _authManager.getAccessToken();

        //Attach access_token to request
        Request newRequest = originalRequest
                .newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .build();

        Response response = chain.proceed(newRequest);
        //If token expired (401), refresh token
        if (response.code() == 401) {
            response.close();

            TokenModel tokenModel = getTokens(_authManager.getAccessToken(), _authManager.getRefreshToken());

            if (tokenModel != null) {
                _authManager.saveTokens(tokenModel.getAccessToken(), tokenModel.getRefreshToken()); //save new tokens

                Request retryRequest = originalRequest
                        .newBuilder()
                        .header("Authorization", "Bearer" + tokenModel.getAccessToken())
                        .build();

                return chain.proceed(retryRequest); //send original api
            }
        }
        return response;
    }

    private TokenModel getTokens(String accessToken, String refreshToken) {
        Log.d("RefreshToken", "");
        try {
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json")
                    , "{" +
                            "\"accessToken\":\"" + accessToken + "\"," +
                            " \"refreshToken\":\"" + refreshToken + "\"" +
                            "}");

            Request request = new Request.Builder()
                    .url(BASE_URL + "api/authentication/refresh")
                    .post(requestBody)
                    .build();


            Response response = new OkHttpClient().newCall(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                String json = response.body().string();
                JSONObject jsonObject = new JSONObject(json);

                TokenModel tokenModel = new TokenModel();
                tokenModel.setAccessToken(jsonObject.getString("accessToken"));
                tokenModel.setRefreshToken(jsonObject.getString("refreshToken"));

                return tokenModel;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }
}
