package com.example.findnest.model.request.authentication;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginRequest {
    private  String userName;
    private  String password;
}
