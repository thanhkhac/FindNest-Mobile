package com.example.findnest.model.response.plan;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanDetailRes {
    private   int id;
    private  int priorityLevel;
    private  int duration;
    private BigDecimal price;
}
