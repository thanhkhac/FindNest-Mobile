package com.example.findnest.api;

import com.example.findnest.model.request.authentication.LoginRequest;
import com.example.findnest.model.response.response.TokenModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IAuthenticationService {
    @POST("api/authentication/login")
    Call<TokenModel> login(@Body LoginRequest request);

    @POST("api/authentication/refresh")
    Call<TokenModel> refreshToken(@Body TokenModel token);
}
