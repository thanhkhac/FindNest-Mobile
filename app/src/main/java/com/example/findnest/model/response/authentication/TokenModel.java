package com.example.findnest.model.response.authentication;

public class TokenModel {
    private String accessToken;
    private String refreshToken;

    public TokenModel() {
    }



    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
