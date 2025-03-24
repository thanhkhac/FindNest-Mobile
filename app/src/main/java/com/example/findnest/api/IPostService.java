package com.example.findnest.api;

import com.example.findnest.model.dto.PostDto;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface IPostService {
    @GET("api/post/{id}")
    Call<PostDto> getPostDetail (@Path("id") String postId);
}
