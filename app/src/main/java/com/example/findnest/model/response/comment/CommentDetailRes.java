package com.example.findnest.model.response.comment;


import androidx.annotation.Size;

import com.example.findnest.model.response.user_for_public.UserForPublicDetailRes;

import org.jetbrains.annotations.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDetailRes {
    private UUID id;
    private UUID postId;
    private UUID parentId;
    private String content;
    private UserForPublicDetailRes createdBy;
    private String createdAt;
    private int replyCount;
    private List<CommentDetailRes> replies = new ArrayList<>();
}

