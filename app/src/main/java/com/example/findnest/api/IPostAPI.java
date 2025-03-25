package com.example.findnest.api;

import com.example.findnest.model.response.post.PostDetailRes;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface IPostAPI {
    @GET("api/post/{id}")
    Call<PostDetailRes> getPostDetail (@Path("id") String postId);
}
