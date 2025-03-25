package com.example.findnest.model.requestdtos;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import okhttp3.MultipartBody;

@Data
@SuperBuilder
public class PostForManipulationRequest
{
    private String title;
    private BigDecimal price = BigDecimal.ZERO;
    private Boolean isNegotiatedPrice = false;
    private String address;
    private Integer area;
    private String description;
    private Double latitude;
    private Double longitude;
    private String wardCode;
    private Integer bedRoomCount;
    private Integer bathRoomCount;
    private List<FileForCreateUpdateRequest> images;
    private MultipartBody.Part image360;
    private Boolean isAiDescription = false;
}
