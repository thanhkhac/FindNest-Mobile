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
    private BigDecimal price;
    private boolean isNegotiatedPrice;
    private String address;
    private int area;
    private String description;
    private Double latitude;
    private Double longitude;
    private String thumbnail;
    private boolean isHidden;
    private Boolean isApproved;
    private RegionDto ward;
    private RegionDto district;
    private RegionDto province;
    private int bedRoomCount;
    private int bathRoomCount;
    private int planPriority;
    private List<PlanDto> plans;
    private String image360;
    private String panorama;
    private List<MediaDto> images;
    private UserForPublicDto createdUser;


}
