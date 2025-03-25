package com.example.findnest.model;

import java.math.BigDecimal;
import java.sql.Date;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    private String id;
    private String title;
    private long price;
    private boolean isNegotiatedPrice;
    private String address;
    private int area;
    private String description;
    private float latitute;
    private float longtitude;
    private boolean isHidden;
    private boolean isApproved;
    private String wardCode;
    private String districtCode;
    private String provinceCode;
    private int bedRoomCount;
    private int bathRoomCount;
    private String createdAt;
    private Date updatedAt;
    private String createdBy;
    private String updatedBy;
    private String deletedBy;
    private boolean isDeleted;
    private int planPriority;
    private String regionAddress;
    private String thumbnail;

}
