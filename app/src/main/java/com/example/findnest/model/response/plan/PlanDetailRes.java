package com.example.findnest.model.response.plan;

import java.math.BigDecimal;

import lombok.*;


@Data
@Builder
@AllArgsConstructor
public class PlanDetailRes {
    private   int id;
    private  int priorityLevel;
    private  int duration;
    private BigDecimal price;
}
