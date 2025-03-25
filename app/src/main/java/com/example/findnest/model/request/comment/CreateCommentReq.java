package com.example.findnest.model.request.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateCommentReq {
    @NotBlank
    private String postId;
    private String parentId;
    @NotBlank
    private String content;
}
