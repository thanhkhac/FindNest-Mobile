package com.example.findnest.model.request.authentication;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterModel {
    private String fullName;
    private String userName;
    private String password;
    private String email;
}
