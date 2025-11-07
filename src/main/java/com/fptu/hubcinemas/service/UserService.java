package com.fptu.hubcinemas.service;

import com.fptu.hubcinemas.config.DebugModeConfig;
import com.fptu.hubcinemas.model.UserInfo;
import com.fptu.hubcinemas.model.enums.UserRole;
import com.fptu.hubcinemas.repository.UserRepository;
import com.fptu.hubcinemas.utils.CustomLogger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UserService implements UserDetailsService {

    public static final CustomLogger logger =
            new CustomLogger(LoggerFactory.getLogger(UserService.class),
            DebugModeConfig.SERVICE_LAYER);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final long RESET_TOKEN_EXPIRY_MINUTES = 60;


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserInfo registerUser(String fullName,
                             String password,
                             String email,
                             String role,
                             String phoneNumber,
                             LocalDate dateOfBirth) {

        logger.info("Bắt đầu đăng ký user: email={}, role={}", email, role);

        // 1️⃣ Validate uniqueness
        Optional<UserInfo> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Kiểm tra định dạng email
        if (!role.equals("customer") && (email == null || !EMAIL_PATTERN.matcher(email).matches())) {
            logger.error("Invalid email: {}", email);
            throw new IllegalArgumentException("Invalid email.");
        }

        UserInfo user = new UserInfo();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setDateOfBirth(dateOfBirth);
        user.setRole(role);
        user.setActive(false); // not yet verified
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());

        // Tạo token xác nhận
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);

        try {
            UserInfo savedUser = userRepository.save(user);

            // Kích hoạt user ngay lập tức cho mục đích thử nghiệm
            savedUser.setActive(true);
            userRepository.save(savedUser);

            logger.info("New user created: id={}, email={}, role={}",
                    savedUser.getId(),
                    savedUser.getEmail(),
                    savedUser.getRole());

            return savedUser;
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage());
            throw new RuntimeException("Error creating user: " + e.getMessage());
        }
    }

    public boolean resetPassword(String token, String newPassword) {
        logger.info("Processing password reset with token: {}", token);

        UserInfo user = userRepository.findByResetPasswordToken(token);
        if (user != null && user.getResetPasswordExpiry() != null && user.getResetPasswordExpiry().isAfter(Instant.now())) {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            user.setResetPasswordToken(null);
            user.setResetPasswordExpiry(null);
            userRepository.save(user);
            logger.info("Password reset successful for user: {}", user.getEmail());
            return true;
        }

        logger.warn("Password reset failed, invalid or expired token: {}", token);
        return false;
    }

    public UserInfo updateUserProfile(String publicId, String fullName, String dateOfBirth, String phoneNumber) {
        UserInfo user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + publicId));

        if (fullName != null) {
            user.setFullName(fullName);
        }
        if (dateOfBirth != null) {
            try {
                user.setDateOfBirth(LocalDate.parse(dateOfBirth));
            } catch (Exception e) {
                throw new IllegalArgumentException("Wrong date format");
            }
        }
        if (phoneNumber != null) {
            user.setPhoneNumber(phoneNumber);
        }

        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Fetch user from the database by email (username)
        Optional<UserInfo> userInfo = userRepository.findByEmail(email);

        if (userInfo.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        // Convert UserInfo to UserDetails (UserInfoDetails)
        UserInfo user = userInfo.get();

        // Create authorities from the user's role
        List<GrantedAuthority> authorities = user.getRole() != null
                ? List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
                : Collections.emptyList();

        return new User(user.getEmail(), user.getPasswordHash(), authorities);
    }
}
