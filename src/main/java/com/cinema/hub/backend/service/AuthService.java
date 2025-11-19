package com.cinema.hub.backend.service;

import com.cinema.hub.backend.dto.auth.ForgotPasswordRequestDto;
import com.cinema.hub.backend.dto.auth.RegisterRequestDto;
import com.cinema.hub.backend.dto.auth.ResetPasswordRequestDto;
import com.cinema.hub.backend.dto.auth.VerifyResetTokenRequestDto;
import com.cinema.hub.backend.entity.PasswordResetToken;
import com.cinema.hub.backend.entity.Role;
import com.cinema.hub.backend.entity.UserAccount;
import com.cinema.hub.backend.repository.PasswordResetTokenRepository;
import com.cinema.hub.backend.repository.RoleRepository;
import com.cinema.hub.backend.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final String DEFAULT_ROLE = "User";
    private static final int RESET_TOKEN_EXPIRY_MINUTES = 15;

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final Random random = new Random();

    public UserAccount register(RegisterRequestDto request) {
        validateRegistration(request);

        if (userAccountRepository.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email đã được sử dụng!");
        }

        String roleName = StringUtils.hasText(request.getRole()) ? request.getRole() : DEFAULT_ROLE;
        Role role = roleRepository.findByNameIgnoreCase(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

        UserAccount user = UserAccount.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .active(true)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        UserAccount saved = userAccountRepository.save(user);
        mailService.sendRegistrationConfirmation(saved.getEmail(), saved.getFullName());
        return saved;
    }

    public PasswordResetToken createPasswordResetToken(ForgotPasswordRequestDto request) {
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        String token = String.format("%06d", random.nextInt(1_000_000));

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(RESET_TOKEN_EXPIRY_MINUTES))
                .build();

        PasswordResetToken saved = passwordResetTokenRepository.save(resetToken);
        mailService.sendPasswordReset(user.getEmail(), token);
        log.info("Password reset token for {} is {}", user.getEmail(), token);
        return saved;
    }

    public void resetPassword(ResetPasswordRequestDto request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password confirmation does not match");
        }

        UserAccount user = userAccountRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        PasswordResetToken token = passwordResetTokenRepository
                .findTopByUserAndTokenAndUsedAtIsNullAndExpiresAtAfter(
                        user,
                        request.getToken(),
                        OffsetDateTime.now(ZoneOffset.UTC))
                .orElseThrow(() -> new IllegalArgumentException("Mã nhập không hợp lệ"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userAccountRepository.save(user);

        token.setUsedAt(OffsetDateTime.now(ZoneOffset.UTC));
        passwordResetTokenRepository.save(token);
    }

    private void validateRegistration(RegisterRequestDto request) {
        if (!StringUtils.hasText(request.getPassword()) ||
                !request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }
    }

    public void verifyResetToken(VerifyResetTokenRequestDto request) {
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        passwordResetTokenRepository
                .findTopByUserAndTokenAndUsedAtIsNullAndExpiresAtAfter(
                        user,
                        request.getToken(),
                        OffsetDateTime.now(ZoneOffset.UTC))
                .orElseThrow(() -> new IllegalArgumentException("Mã nhập không hợp lệ"));
    }
}
