package com.example.findnest.model.responsedtos;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostDetailResponse {
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
    private RegionResponse ward;
    private RegionResponse district;
    private RegionResponse province;
    private int bedRoomCount;
    private int bathRoomCount;
    private int planPriority;
    private List<PlanResponse> plans;
    private String image360;
    private String panorama;
    private List<MediaResponse> images;
    private UserForPublicResponse createdUser;
}