package com.example.findnest.api;

import com.example.findnest.model.request.user.ChangePasswordReq;
import com.example.findnest.model.request.user.ForgotPasswordReq;
import com.example.findnest.model.request.user.ResetPasswordReq;
import com.example.findnest.model.request.user.UserContactInfoReq;
import com.example.findnest.model.response.authentication.TokenModel;
import com.example.findnest.model.response.post.PostDetailRes;
import com.example.findnest.model.response.user_for_public.UserForPublicDetailRes;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface IUserAPI {
    @GET("api/user/manage/info")
    Call<UserForPublicDetailRes> getUser();

    @PUT("api/user/manage/contact-info")
    Call<Void> updateContactInfo(
            @Body
            UserContactInfoReq request);

    @PUT("api/user/manage/password")
    Call<Void> changePassword(
            @Body
            ChangePasswordReq request);

    @POST("api/authentication/forgot-password")
    Call<Void> sendEmailForgotPassword(
            @Body
            ForgotPasswordReq request);

    @POST("api/authentication/reset-password")
    Call<Void> resetPassword(
            @Body
            ResetPasswordReq request);

    @Multipart
    @PUT("api/user/manage/avatar")
    Call<Void> changeAvatar(
            @Part
            MultipartBody.Part avatarFile);
}
