package com.fptu.hubcinemas.service;

import com.fptu.hubcinemas.config.DebugModeConfig;
import com.fptu.hubcinemas.model.User;
import com.fptu.hubcinemas.model.enums.UserRole;
import com.fptu.hubcinemas.repository.UserRepository;
import com.fptu.hubcinemas.utils.CustomLogger;
import jakarta.mail.MessagingException;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
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
    public User registerUser(String username,
                                     String password,
                                     String email,
                                     String role,
                                     String phoneNumber,
                                     LocalDate dateOfBirth) throws MessagingException, IOException {

        logger.info("Bắt đầu đăng ký user: username={}, role={}", username, role);


        // Kiểm tra định dạng email
        if (!role.equals("customer") && (email == null || !EMAIL_PATTERN.matcher(email).matches())) {
            logger.error("Email cá nhân không hợp lệ: {}", email);
            throw new IllegalArgumentException("Email cá nhân không hợp lệ.");
        }

        // Kiểm tra username tồn tại
        if (userRepository.findByUsername(username).isPresent()) {
            logger.error("Tài khoản đã tồn tại: username={}", username);
            throw new IllegalArgumentException("Tài khoản đã tồn tại.");
        }

        // Kiểm tra email tồn tại
        if (email != null && userRepository.findByEmail(email).isPresent()) {
            logger.error("Email đã được sử dụng: email={}", email);
            throw new IllegalArgumentException("Email đã được sử dụng.");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(UserRole.CUSTOMER.toString());
        user.setPhoneNumber(phoneNumber);
        user.setIsActive(true);

        // Tạo token xác nhận
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);

        try {
            User savedUser = userRepository.save(user);

            savedUser.setVerified(true);
            savedUser.setIsActive(true);
            userRepository.save(savedUser);

            logger.info("Đã lưu user vào cơ sở dữ liệu: id={}, username={}, role={}", savedUser.getId(), savedUser.getUsername(), savedUser.getRole());

            return savedUser;
        } catch (Exception e) {
            logger.error("Lỗi khi lưu user vào cơ sở dữ liệu: {}", e.getMessage());
            throw new RuntimeException("Lỗi khi lưu user: " + e.getMessage());
        }
    }

    public boolean resetPassword(String token, String newPassword) {
        logger.info("Processing password reset with token: {}", token);

        User user = userRepository.findByResetPasswordToken(token);
        if (user != null && user.getResetPasswordExpiry() != null && user.getResetPasswordExpiry().isAfter(Instant.now())) {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            user.setResetPasswordToken(null);
            user.setResetPasswordExpiry(null);
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
            logger.info("Password reset successful for user: {}", user.getUsername());
            return true;
        }

        logger.warn("Password reset failed, invalid or expired token: {}", token);
        return false;
    }

    public User updateUserProfile(String username, String fullName, String dateOfBirth, String phoneNumber) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

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

        user.setUpdatedAt(Instant.now());
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<GrantedAuthority> authorities = getAuthorities(user);

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                authorities
        );
    }

    private List<GrantedAuthority> getAuthorities(User user) {
        String role = user.getRole();
        if (role == null || role.isEmpty()) {
            return Collections.emptyList();
        }
        // Ensure role has ROLE_ prefix
        String prefixed = role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();
        return Collections.singletonList(new SimpleGrantedAuthority(prefixed));
    }
}
