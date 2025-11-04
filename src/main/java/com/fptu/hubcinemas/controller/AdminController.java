package com.fptu.hubcinemas.controller;

import com.fptu.hubcinemas.config.DebugModeConfig;
import com.fptu.hubcinemas.config.ApiEndpoints;
import com.fptu.hubcinemas.model.User;
import com.fptu.hubcinemas.service.AdminService;
import com.fptu.hubcinemas.utils.CustomLogger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    public static final CustomLogger logger =
            new CustomLogger(LoggerFactory.getLogger(AdminController.class),
            DebugModeConfig.CONTROLLER_LAYER);

    @GetMapping(ApiEndpoints.ADMIN_USERS)
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("Fetching all users");
        List<User> users = adminService.getAllUsers();
        logger.info("Retrieved {} users", users.size());
        return ResponseEntity.ok(users);
    }
}
