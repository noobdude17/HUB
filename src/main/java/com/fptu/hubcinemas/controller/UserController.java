package com.fptu.hubcinemas.controller;

import com.fptu.hubcinemas.config.ApiEndpoints;
import com.fptu.hubcinemas.config.DebugModeConfig;
import com.fptu.hubcinemas.dto.RegisterRequestDto;
import com.fptu.hubcinemas.model.User;
import com.fptu.hubcinemas.model.enums.UserRole;
import com.fptu.hubcinemas.service.UserService;
import com.fptu.hubcinemas.utils.CustomLogger;
import jakarta.mail.MessagingException;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@CrossOrigin
@RestController
public class UserController {

}
