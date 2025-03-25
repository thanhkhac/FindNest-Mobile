package com.example.findnest.api;

import com.example.findnest.model.Post;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IPostService {
    @GET("api/post")
    Call<List<Post>> getPosts(
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("isNegotiatedPrice") Boolean isNegotiatedPrice,
            @Query("isAllPrice") Boolean isAllPrice,
            @Query("minArea") Double minArea,
            @Query("maxArea") Double maxArea,
            @Query("provinceCode") String provinceCode,
            @Query("districtCode") String districtCode,
            @Query("pageNumber") int page,
            @Query("pageSize") int size
    );
}
