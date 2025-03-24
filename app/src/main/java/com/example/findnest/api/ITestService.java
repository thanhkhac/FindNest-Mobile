package com.example.findnest.api;

import com.example.findnest.model.Plan;
import com.example.findnest.model.authentication.TokenModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface ITestService {
    @GET("api/Plan")
    Call<List<Plan>> getPlan(@Header("Authorization") TokenModel token);

}
