package com.example.findnest.api;

import com.example.findnest.model.request.plan.BuyPlanReq;
import com.example.findnest.model.response.plan.PlanDetailRes;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface IPlanAPI {

    @GET("api/Plan")
    Call<List<PlanDetailRes>> getPlan();

    @POST("api/post/{postId}/plans")
    Call<Void> buyPlan(
            @Path("postId")
            String postId,
            @Body
            BuyPlanReq request);
}
