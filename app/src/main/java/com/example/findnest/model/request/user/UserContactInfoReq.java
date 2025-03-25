package com.example.findnest.model.request.user;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserContactInfoReq {
    private String fullName;
    private String contactPhoneNumber;
    private String zalo;
}
