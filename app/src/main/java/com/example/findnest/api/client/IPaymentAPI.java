package com.example.findnest.api.client;

import com.example.findnest.model.request.payment.CreateQRReq;
import com.example.findnest.model.response.transaction.PaymentQRRes;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IPaymentAPI {
    @POST("api/payment/payment/qrcode")
    Call<PaymentQRRes> createQR(@Body CreateQRReq rq);
}
