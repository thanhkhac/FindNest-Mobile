package com.example.findnest.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionDto {
    public String code;
    public String name;
    public String nameEn;
    public String fullName;
    public String fullNameEn;
    public String codeName;


}
