package com.example.findnest.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserForPublicDto {
    public String id;
    public String fullName;
    public String avatar;
    public String contactPhoneNumber;
    public String zalo;
    public String facebook;
}
