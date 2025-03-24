package com.example.findnest.api;

import com.example.findnest.model.authentication.TokenModel;
import com.example.findnest.model.authentication.LoginRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface IAuthenticationService {
    @POST("api/authentication/login")
    Call<TokenModel> login(@Body LoginRequest request);

    @POST("api/authentication/refresh")
    Call<TokenModel> refreshToken(@Body TokenModel token);
}