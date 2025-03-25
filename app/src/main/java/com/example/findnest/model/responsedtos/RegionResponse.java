package com.example.findnest.model.responsedtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegionResponse
{
    private String code;
    private String name;
    private String nameEn;
    private String fullName;
    private String fullNameEn;
    private String codeName;
}
