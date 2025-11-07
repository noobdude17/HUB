package com.fptu.hubcinemas.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class UserInfoDetails implements UserDetails {

    private final String email; // Changed from 'name' to 'email' for clarity
    private final String password;
    private final List<GrantedAuthority> authorities;

    public UserInfoDetails(UserInfo user) {
        this.email = user.getEmail(); // Use email as username
        this.password = user.getPasswordHash();
        // Handle potential null role
        this.authorities = user.getRole() != null
                ? List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
                : Collections.emptyList();
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
