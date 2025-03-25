package com.example.findnest.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterRangeDTO {
    private String text;
    private Double minValue;
    private Double maxValue;
}
