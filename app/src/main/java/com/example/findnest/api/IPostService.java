package com.example.findnest.api;

import com.example.findnest.model.Post;
import com.example.findnest.model.requestdtos.FileForCreateUpdateRequest;
import com.example.findnest.model.responsedtos.PostDetailResponse;

import java.util.List;
import java.util.UUID;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface IPostService {
    @Multipart
    @POST("api/post")
    Call<PostDetailResponse> createPost(
            @Part("title") RequestBody title,
            @Part("price") RequestBody price,
            @Part("isNegotiatedPrice") RequestBody isNegotiatedPrice,
            @Part("address") RequestBody address,
            @Part("area") RequestBody area,
            @Part("description") RequestBody description,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part("wardCode") RequestBody wardCode,
            @Part("bedRoomCount") RequestBody bedRoomCount,
            @Part("bathRoomCount") RequestBody bathRoomCount,
            @Part List<MultipartBody.Part> images,
            @Part("isAiDescription") RequestBody isAiDescription,
            @Part MultipartBody.Part thumbnail
    );

    @Multipart
    @PUT("api/post/{id}")
    Call<PostDetailResponse> updatePost(
            @Path("id") UUID id,
            @Part("title") RequestBody title,
            @Part("price") RequestBody price,
            @Part("isNegotiatedPrice") RequestBody isNegotiatedPrice,
            @Part("address") RequestBody address,
            @Part("area") RequestBody area,
            @Part("description") RequestBody description,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part("wardCode") RequestBody wardCode,
            @Part("bedRoomCount") RequestBody bedRoomCount,
            @Part("bathRoomCount") RequestBody bathRoomCount,
            @Part("isAiDescription") RequestBody isAiDescription,
            @Part MultipartBody.Part thumbnail
    );

    @GET("api/post/{id}")
    Call<PostDetailResponse> getPost(
            @Path("id")
            UUID postId);

    @DELETE("api/post/{id}")
    Call<Void> deletePost(
            @Path("id")
            UUID postId);

    @GET("api/post")
    Call<List<Post>> getPosts(
            @Query("pageNumber")
            int page,
            @Query("pageSize")
            int size
    );

    @GET("api/post")
    Call<List<Post>> getPosts(
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("isNegotiatedPrice") Boolean isNegotiatedPrice,
            @Query("isAllPrice") Boolean isAllPrice,
            @Query("minArea") Integer minArea,
            @Query("maxArea") Integer maxArea,
            @Query("provinceCode") String provinceCode,
            @Query("districtCode") String districtCode,
            @Query("wardCode") String wardCode,
            @Query("pageNumber") int page,
            @Query("pageSize") int size
    );

}


