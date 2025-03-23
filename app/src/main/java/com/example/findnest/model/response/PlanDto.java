package com.example.findnest.model.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanDto {
    public   int id;
    public  int priorityLevel;
    public  int duration;
    public BigDecimal price;


}
