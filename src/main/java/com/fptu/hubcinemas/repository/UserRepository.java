package com.fptu.hubcinemas.repository;

import com.fptu.hubcinemas.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserInfo, Long> {
    Optional<UserInfo> findByEmail(String email);
    UserInfo findByVerificationToken(String token);
    UserInfo findByResetPasswordToken(String token);
    List<UserInfo> findByRole(String role);
    Optional<UserInfo> findByPublicId(String publicId);
    List<UserInfo> findByRoleAndIsActiveFalse(String role);
    List<UserInfo> findByIsActiveTrue();
}
