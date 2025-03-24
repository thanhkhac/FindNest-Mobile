package com.example.findnest.api;

import com.example.findnest.model.requestdtos.FileForCreateUpdateRequest;
import com.example.findnest.model.responsedtos.PostDetailResponse;

import java.util.List;
import java.util.UUID;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface IPostService
{
        @Multipart
        @POST("/api/post")
        Call<Void> createPost(
                @Part MultipartBody.Part thumbnail,
                @Part MultipartBody.Part image360,
                @Part("title") String title,
                @Part("price") double price,
                @Part("isNegotiatedPrice") boolean isNegotiatedPrice,
                @Part("address") String address,
                @Part("area") int area,
                @Part("description") String description,
                @Part("latitude") double latitude,
                @Part("longitude") double longitude,
                @Part("wardCode") String wardCode,
                @Part("bedRoomCount") int bedRoomCount,
                @Part("bathRoomCount") int bathRoomCount,
                @Part("images") List<FileForCreateUpdateRequest> images,
                @Part("isAiDescription") boolean isAiDescription
        );

    @Multipart
    @PUT("/api/post/{id}")
    Call<Void> updatePost(
            @Path("id") UUID postId,
            @Part MultipartBody.Part thumbnail,
            @Part MultipartBody.Part image360,
            @Query("title") String title,
            @Query("price") double price,
            @Query("isNegotiatedPrice") boolean isNegotiatedPrice,
            @Query("address") String address,
            @Query("area") int area,
            @Query("description") String description,
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("wardCode") String wardCode,
            @Query("bedRoomCount") int bedRoomCount,
            @Query("bathRoomCount") int bathRoomCount,
            @Query("images") List<FileForCreateUpdateRequest> images,
            @Query("isAiDescription") boolean isAiDescription
    );

    @GET("/api/post/{id}")
    Call<PostDetailResponse> getPost(@Path("id") UUID postId);


}
