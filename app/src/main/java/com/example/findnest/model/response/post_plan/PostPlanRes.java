package com.example.findnest.model.response.post_plan;

import com.example.findnest.model.response.plan.PlanDetailRes;

import java.util.Date;
import java.util.UUID;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostPlanRes {
    private String id;
    private String postId;
    private Integer planId;
    private PlanDetailRes plan;
    private String startDate;
    private String endDate;
    private String createdAt;
}
