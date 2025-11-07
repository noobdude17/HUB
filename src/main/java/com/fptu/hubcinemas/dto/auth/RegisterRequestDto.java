package com.fptu.hubcinemas.dto.auth;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class RegisterRequestDto {
    @NotBlank
    @Size(min=2, max=100)
    private String fullName;

    @NotBlank
    @Size(min=8, max=100)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character.")
    private String password;

    @NotBlank
    @Size(min=8, max=100)
    private String retypePassword;

    @NotBlank
    @Email
    @Size(max=100)
    private String email;

    private LocalDate dateOfBirth;

    private String role;

    private String phoneNumber;
}
