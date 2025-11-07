package com.fptu.hubcinemas.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeProfileDto {
    @NotBlank
    @Size(min=2, max=100)
    private String fullName;

    private String avatarUrl;

    @NotBlank
    @Size(min=10, max=10)
    private String phoneNumber;

    private String dateOfBirth;
}
