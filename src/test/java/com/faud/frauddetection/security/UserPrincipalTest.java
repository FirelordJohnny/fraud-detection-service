package com.faud.frauddetection.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserPrincipalTest {

    private UserPrincipal userPrincipal;
    private final String username = "testuser";
    private final String userId = "user123";
    private final List<GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_ADMIN"),
            new SimpleGrantedAuthority("VIEW_DATA")
    );

    @BeforeEach
    void setUp() {
        userPrincipal = new UserPrincipal(username, userId, authorities);
    }

    @Test
    void constructor_shouldAssignFieldsCorrectly() {
        assertEquals(username, userPrincipal.getUsername());
        assertEquals(userId, userPrincipal.getUserId());
        assertEquals(authorities, userPrincipal.getAuthorities());
    }

    @Test
    void getName_shouldReturnUsername() {
        assertEquals(username, userPrincipal.getName());
    }

    @Test
    void getUsername_shouldReturnUsername() {
        assertEquals(username, userPrincipal.getUsername());
    }

    @Test
    void getUserId_shouldReturnUserId() {
        assertEquals(userId, userPrincipal.getUserId());
    }

    @Test
    void getAuthorities_shouldReturnAuthorities() {
        Collection<? extends GrantedAuthority> returnedAuthorities = userPrincipal.getAuthorities();
        assertNotNull(returnedAuthorities);
        assertEquals(2, returnedAuthorities.size());
        assertTrue(returnedAuthorities.containsAll(authorities));
    }

    @Test
    void hasRole_shouldReturnTrueForExistingRole() {
        assertTrue(userPrincipal.hasRole("ADMIN"));
    }

    @Test
    void hasRole_shouldReturnFalseForNonExistingRole() {
        assertFalse(userPrincipal.hasRole("USER"));
    }
    
    @Test
    void hasRole_shouldBeCaseSensitive() {
        assertFalse(userPrincipal.hasRole("admin"));
    }

    @Test
    void hasAuthority_shouldReturnTrueForExistingAuthority() {
        assertTrue(userPrincipal.hasAuthority("ROLE_ADMIN"));
        assertTrue(userPrincipal.hasAuthority("VIEW_DATA"));
    }

    @Test
    void hasAuthority_shouldReturnFalseForNonExistingAuthority() {
        assertFalse(userPrincipal.hasAuthority("DELETE_DATA"));
        assertFalse(userPrincipal.hasAuthority("ROLE_USER"));
    }

    @Test
    void hasAuthority_shouldBeCaseSensitive() {
        assertFalse(userPrincipal.hasAuthority("view_data"));
    }
    
    @Test
    void principalWithNoAuthorities() {
        UserPrincipal principal = new UserPrincipal("noauth_user", "user000", Collections.emptyList());
        assertFalse(principal.hasRole("ANY_ROLE"));
        assertFalse(principal.hasAuthority("ANY_AUTHORITY"));
        assertEquals(0, principal.getAuthorities().size());
    }
} 