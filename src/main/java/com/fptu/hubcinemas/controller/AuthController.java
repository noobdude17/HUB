package com.fptu.hubcinemas.controller;

import com.fptu.hubcinemas.config.ApiEndpoints;
import com.fptu.hubcinemas.config.DebugModeConfig;
import com.fptu.hubcinemas.dto.LoginRequestDto;
import com.fptu.hubcinemas.dto.RegisterRequestDto;
import com.fptu.hubcinemas.model.User;
import com.fptu.hubcinemas.model.enums.UserRole;
import com.fptu.hubcinemas.service.UserService;
import com.fptu.hubcinemas.utils.CustomLogger;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@CrossOrigin
@RestController
public class AuthController {

    public static final CustomLogger logger =
            new CustomLogger(LoggerFactory.getLogger(AuthController.class),
                    DebugModeConfig.CONTROLLER_LAYER);

    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }

    @PostMapping(ApiEndpoints.AUTH_REGISTER)
    public ResponseEntity<?> registerCustomer(@RequestBody RegisterRequestDto request)
            throws MessagingException, IOException {
        logger.info(
                "Xử lý yêu cầu đăng ký cho username: {}, role: {}",
                request.getUsername(),
                request.getRole());

        if (!request.getPassword().equals(request.getRetypePassword())) {
            logger.warn("Mật khẩu không khớp cho username: {}", request.getUsername());
            return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu không khớp."));
        }

        try {
            User user = userService.registerUser(
                    request.getUsername(),
                    request.getPassword(),
                    request.getEmail(),
                    request.getRole(),
                    request.getPhoneNumber(),
                    request.getDateOfBirth()
            );
            String successMessage = user.getRole().equals(UserRole.CUSTOMER.toString()) ?
                    "Đăng ký thành công. Vui lòng kiểm tra email để xác nhận tài khoản." :
                    "Đăng ký thành công. Quản trị viên sẽ phê duyệt tài khoản của bạn sớm nhất có thể.";
            logger.info("Đăng ký thành công cho username: {}", request.getUsername());
            return ResponseEntity.ok(Map.of("message", successMessage));

        } catch (IllegalArgumentException e) {
            logger.error("Đăng ký thất bại: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(ApiEndpoints.AUTH_LOGIN)
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request, HttpSession session) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);
            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
            if (request.isRememberMe()) {
                logger.info("Remember Me enabled for username: {}", request.getUsername());
            }

            logger.info("Login successful for username: {}", request.getUsername());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Login failed for username: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Wrong username or password. Please try again.");
        }
    }

    @PostMapping(ApiEndpoints.AUTH_LOGOUT)
    public ResponseEntity<?> logout(HttpSession session) {
        logger.task("Processing logout, session ID: {}", session.getId());
        SecurityContextHolder.clearContext();
        session.invalidate();
        return ResponseEntity.ok("Logged out successfully.");
    }
}
