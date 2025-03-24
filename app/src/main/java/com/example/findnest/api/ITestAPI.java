package com.example.findnest.api;

import com.example.findnest.model.response.plan.PlanDetailRes;
import com.example.findnest.model.response.authentication.TokenModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface ITestAPI {
    @GET("api/Plan")
    Call<List<PlanDetailRes>> getPlan(@Header("Authorization") TokenModel token);

}
