package com.seatsync.backend.auth;

import jakarta.validation.constraints.NotNull;

public class RefreshTokenRequest {

    @NotNull
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
