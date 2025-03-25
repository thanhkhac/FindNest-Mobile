package com.example.findnest.model.responsedtos;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlanResponse
{
    private int id;
    private int priorityLevel;
    private int duration;
    private BigDecimal price;
}
