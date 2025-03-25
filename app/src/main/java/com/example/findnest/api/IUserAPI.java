package com.example.findnest.api;

import com.example.findnest.model.Post;
import com.example.findnest.model.request.user.ChangePasswordReq;
import com.example.findnest.model.request.user.UserContactInfoReq;
import com.example.findnest.model.response.user_for_public.UserForPublicDetailRes;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

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

    @Multipart
    @PUT("api/user/manage/avatar")
    Call<Void> changeAvatar(
            @Part
            MultipartBody.Part avatarFile);

    @GET("api/user/manage/post")
    Call<List<Post>> getMyPosts(
            @Query("pageNumber")
            int page,
            @Query("pageSize")
            int size);
}
