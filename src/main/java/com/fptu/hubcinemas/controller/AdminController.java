package com.fptu.hubcinemas.controller;

import com.fptu.hubcinemas.config.DebugModeConfig;
import com.fptu.hubcinemas.config.ApiEndpoints;
import com.fptu.hubcinemas.model.UserInfo;
import com.fptu.hubcinemas.service.AdminService;
import com.fptu.hubcinemas.utils.CustomLogger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping(ApiEndpoints.ADMIN_USERS)
    public ResponseEntity<List<?>> getAllUsers() {
        List<?> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping(ApiEndpoints.ADMIN_USER_BY_ID)
    public ResponseEntity<Optional<?>> getAllActiveUsers(@PathVariable String id) {
        Optional<UserInfo> user = adminService.findByPublicId(id);
        return ResponseEntity.ok(user);
    }
}
