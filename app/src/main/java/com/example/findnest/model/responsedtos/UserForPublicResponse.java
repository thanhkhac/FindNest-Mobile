package com.example.findnest.model.responsedtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserForPublicResponse
{
    private String id;
    private String fullName;
    private String avatar;
    private String contactPhoneNumber;
    private String zalo;
    private String facebook;
}
