package com.example.findnest.api;

import com.example.findnest.model.Post;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IPostService {
    @GET("api/post")
    Call<List<Post>> getPosts(
            @Query("pageNumber") int page,
            @Query("pageSize") int size
    );
}
