package com.example.findnest.api;

import com.example.findnest.model.response.post_plan.PostPlanRes;
import com.example.findnest.model.response.transaction.TransactionHistoryRes;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface IMangeAPI {
    @GET("api/user/manage/bought-postplan")
    Call<List<PostPlanRes>> getPostPlanDetail ();

    @GET("api/user/manage/transaction-history")
    Call<List<TransactionHistoryRes>> getTransactionHistory ();
}
