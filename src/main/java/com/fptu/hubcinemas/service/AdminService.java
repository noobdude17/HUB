package com.fptu.hubcinemas.service;

import com.fptu.hubcinemas.config.DebugModeConfig;
import com.fptu.hubcinemas.model.User;
import com.fptu.hubcinemas.repository.UserRepository;
import com.fptu.hubcinemas.utils.CustomLogger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    public static final CustomLogger logger =
            new CustomLogger(LoggerFactory.getLogger(AdminService.class),
                    DebugModeConfig.SERVICE_LAYER);

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getAllActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }

    public Optional<User> findById(Long id){
        return userRepository.findById(id);
    }

    public User save(User user){
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        logger.info("Deleting user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        userRepository.deleteById(id);
        logger.info("User deleted successfully: id={}", id);
    }
}
