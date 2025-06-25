package com.faud.frauddetection.security;

import org.springframework.security.core.GrantedAuthority;

import java.security.Principal;
import java.util.Collection;

/**
 * Custom user principal for JWT authentication
 */
public class UserPrincipal implements Principal {
    
    private final String username;
    private final String userId;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(String username, String userId, Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.userId = userId;
        this.authorities = authorities;
    }

    @Override
    public String getName() {
        return username;
    }

    public String getUsername() {
        return username;
    }

    public String getUserId() {
        return userId;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public boolean hasRole(String role) {
        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    public boolean hasAuthority(String authority) {
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(authority));
    }
} 