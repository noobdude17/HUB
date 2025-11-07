package com.fptu.hubcinemas.controller;

import com.fptu.hubcinemas.config.ApiEndpoints;
import com.fptu.hubcinemas.dto.user.ChangeProfileDto;
import com.fptu.hubcinemas.model.UserInfo;
import com.fptu.hubcinemas.service.UserService;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@CrossOrigin
@RestController
@PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_MANAGER', 'ROLE_ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
}
