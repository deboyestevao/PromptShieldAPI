package com.example.PromptShieldAPI.service;

import com.example.PromptShieldAPI.model.User;
import com.example.PromptShieldAPI.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private CustomUserDetailsService service;

    @Test
    void testLoadUserByUsername_WhenUserExists() {
        User user = new User();
        user.setUsername("admin");
        user.setEmail("admin@example.com");
        user.setPassword("123");
        user.setRole("ADMIN");

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = service.loadUserByUsername("admin@example.com");

        assertEquals("admin", userDetails.getUsername());
        assertEquals("123", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testLoadUserByUsername_WhenUserNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            service.loadUserByUsername("notfound@example.com");
        });
    }

    @Test
    void testLoadUserByUsername_WhenUserIsDeleted() {
        User user = new User();
        user.setUsername("deleted");
        user.setEmail("deleted@example.com");
        user.setPassword("123");
        user.setRole("USER");
        user.softDelete("admin"); // Marcar como deletado

        when(userRepository.findByEmail("deleted@example.com")).thenReturn(Optional.of(user));

        assertThrows(UsernameNotFoundException.class, () -> {
            service.loadUserByUsername("deleted@example.com");
        });
    }
}
