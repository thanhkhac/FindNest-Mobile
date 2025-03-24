package com.example.findnest.model.dto;

import java.math.BigDecimal;

import lombok.*;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanDto {
    public   int id;
    public  int priorityLevel;
    public  int duration;
    public BigDecimal price;
}
