package com.fptu.hubcinemas.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponseDto {
    private String token;
    private String fullName;
    private String role;

    public LoginResponseDto(String token, String fullName, String name) {
        this.token = token;
        this.fullName = fullName;
        this.role = name;
    }

    public LoginResponseDto(String token) {
        this.token = token;
        this.role = role;
    }
}
