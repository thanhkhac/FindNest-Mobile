package com.example.findnest.api;

import com.example.findnest.model.request.comment.CreateCommentReq;
import com.example.findnest.model.response.comment.CommentDetailRes;
import jakarta.validation.Valid;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ICommentAPI {
    @GET("api/comments/post/{id}")
    Call<List<CommentDetailRes>> getCommentsByPostId(@Path("id") String postId);

    @POST("api/comments")
    Call<CommentDetailRes> createComment(@Valid @Body CreateCommentReq rq);
}
