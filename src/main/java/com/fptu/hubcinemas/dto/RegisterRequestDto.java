package com.fptu.hubcinemas.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class RegisterRequestDto {
    // Getters and setters
    private String username;
    private String password;
    private String retypePassword;
    private String email;
    private LocalDate dateOfBirth;
    private String role;
    private String phoneNumber;
}
