package com.example.findnest.model.response.post;

import com.example.findnest.model.response.media.MediaDetailRes;
import com.example.findnest.model.response.plan.PlanDetailRes;
import com.example.findnest.model.response.region.RegionDetailRes;
import com.example.findnest.model.response.user_for_public.UserForPublicDetailRes;

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
public class PostDetailRes {
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
    private RegionDetailRes ward;
    private RegionDetailRes district;
    private RegionDetailRes province;
    private int bedRoomCount;
    private int bathRoomCount;
    private int planPriority;
    private List<PlanDetailRes> plans;
    private String image360;
    private String panorama;
    private List<MediaDetailRes> images;
    private UserForPublicDetailRes createdUser;


}
