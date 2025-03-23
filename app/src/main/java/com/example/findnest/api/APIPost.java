package com.example.findnest.api;

import com.example.findnest.model.response.PostDto;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface APIPost {
    @GET("{id}")
    Call<PostDto> getPostDetail (@Path("id") String postId);
}
