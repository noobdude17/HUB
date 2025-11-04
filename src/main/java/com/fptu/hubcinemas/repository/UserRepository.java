package com.fptu.hubcinemas.repository;

import com.fptu.hubcinemas.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    User findByVerificationToken(String token);
    User findByResetPasswordToken(String token);
    List<User> findByRole(String role);
    Optional<User> findByPublicId(String publicId);
    List<User> findByRoleAndIsActiveFalse(String role);
    List<User> findByIsActiveTrue();
}
