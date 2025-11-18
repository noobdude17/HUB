package com.cinema.hub.backend.service;

import com.cinema.hub.backend.dto.profile.ChangePasswordRequestDto;
import com.cinema.hub.backend.dto.profile.UpdateProfileRequestDto;
import com.cinema.hub.backend.dto.profile.UserProfileDto;
import com.cinema.hub.backend.entity.UserAccount;
import com.cinema.hub.backend.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileDto getProfile(Integer userId) {
        UserAccount user = findUser(userId);
        return mapToDto(user);
    }

    public UserProfileDto updateProfile(Integer userId, UpdateProfileRequestDto request) {
        UserAccount user = findUser(userId);
        user.setFullName(buildFullName(request.getFirstName(), request.getLastName()));
        user.setPhone(request.getPhone());
        UserAccount saved = userAccountRepository.save(user);
        return mapToDto(saved);
    }

    public void changePassword(Integer userId, ChangePasswordRequestDto request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        UserAccount user = findUser(userId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userAccountRepository.save(user);
    }

    private UserAccount findUser(Integer userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
    }

    private UserProfileDto mapToDto(UserAccount user) {
        String[] parts = splitFullName(user.getFullName());
        return UserProfileDto.builder()
                .userId(user.getId())
                .firstName(parts[0])
                .lastName(parts[1])
                .phone(user.getPhone())
                .email(user.getEmail())
                .build();
    }

    private String buildFullName(String firstName, String lastName) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(firstName)) {
            builder.append(firstName.trim());
        }
        if (StringUtils.hasText(lastName)) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(lastName.trim());
        }
        return builder.toString().trim();
    }

    private String[] splitFullName(String fullName) {
        if (!StringUtils.hasText(fullName)) {
            return new String[]{"", ""};
        }
        String[] tokens = fullName.trim().split("\\s+");
        if (tokens.length == 1) {
            return new String[]{"", tokens[0]};
        }
        String lastName = tokens[tokens.length - 1];
        String firstName = String.join(" ", Arrays.copyOf(tokens, tokens.length - 1));
        return new String[]{firstName, lastName};
    }
}
