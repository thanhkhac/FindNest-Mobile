package com.example.findnest.model.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
    private String id;
    private String title;
    public BigDecimal price;
    public boolean isNegotiatedPrice;
    public String address;
    public int area;
    public String description;
    private Double latitude;
    private Double longitude;
    public String thumbnail;
    public boolean isHidden;
    public Boolean isApproved;
    public RegionDto ward;
    public RegionDto district;
    public RegionDto province;
    public int bedRoomCount;
    public int bathRoomCount;
    public int planPriority;
    public List<PlanDto> plans;
    public String image360;
    public String panorama;
    public List<MediaDto> images;
    public UserForPublicDto createdUser;
}
