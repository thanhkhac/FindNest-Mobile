package com.example.findnest.model.response.user_for_public;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserForPublicDetailRes {
    private String id;
    private String fullName;
    private String avatar;
    private String contactPhoneNumber;
    private String zalo;
    private String facebook;
    private BigDecimal balance;
}
