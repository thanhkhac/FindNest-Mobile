package com.example.findnest.model.request.plan;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BuyPlanReq {
    private int planId;
    private String promotionCode;
}
