package com.fptu.hubcinemas.controller;

import com.fptu.hubcinemas.config.ApiEndpoints;
import com.fptu.hubcinemas.config.DebugModeConfig;
import com.fptu.hubcinemas.dto.auth.LoginRequestDto;
import com.fptu.hubcinemas.dto.auth.LoginResponseDto;
import com.fptu.hubcinemas.dto.auth.RegisterRequestDto;
import com.fptu.hubcinemas.model.UserInfo;
import com.fptu.hubcinemas.repository.UserRepository;
import com.fptu.hubcinemas.security.JwtService;
import com.fptu.hubcinemas.service.UserService;
import com.fptu.hubcinemas.utils.CustomLogger;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@CrossOrigin
@RestController
public class AuthController {

    public static final CustomLogger logger =
            new CustomLogger(LoggerFactory.getLogger(AuthController.class),
                    DebugModeConfig.CONTROLLER_LAYER);

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping(ApiEndpoints.AUTH_REGISTER)
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDto request)
            throws MessagingException, IOException {
        logger.info(
                "Xử lý yêu cầu đăng ký cho FullName: {}, role: {}",
                request.getFullName(),
                request.getRole());

        if (!request.getPassword().equals(request.getRetypePassword())) {
            logger.warn("Incorrect password for: {}", request.getFullName());
            return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu không khớp."));
        }

        try {
            UserInfo user = userService.registerUser(
                    request.getFullName(),
                    request.getPassword(),
                    request.getEmail(),
                    request.getRole(),
                    request.getPhoneNumber(),
                    request.getDateOfBirth()
            );

            return ResponseEntity.ok(new Object() {
                public final Long id = user.getId();
                public final String email = user.getEmail();
                public final String fullName = user.getFullName();
                public final String role = user.getRole();
                public final boolean active = user.isActive();
            });

        } catch (IllegalArgumentException e) {
            logger.error("Đăng ký thất bại: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(ApiEndpoints.AUTH_LOGIN)
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
        Optional<UserInfo> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            throw new BadCredentialsException("Invalid email or password");
        }

        UserInfo user = userOpt.get();

        if (!user.isActive()) {
            return ResponseEntity.badRequest().body("Account not active or not verified");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // Authenticate
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(auth);
//
        // Generate JWT
        String token = jwtService.generateToken(user);
//
        // Return response
        return ResponseEntity.ok(new LoginResponseDto(token));
    }

    @PostMapping(ApiEndpoints.AUTH_LOGOUT)
    public ResponseEntity<?> logout() {
        logger.task("Processing logout");
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of(
                "message", "Logged out successfully",
                "instruction", "Please remove the token from client storage"
        ));
    }
}
