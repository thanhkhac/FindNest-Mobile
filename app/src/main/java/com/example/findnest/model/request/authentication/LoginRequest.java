package com.example.findnest.model.request.authentication;

public class LoginRequest {
    private  String userName;
    private  String password;

    public LoginRequest(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }
}
