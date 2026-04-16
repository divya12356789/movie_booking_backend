package com.movieBooking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.movieBooking.model.User;
import com.movieBooking.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void createUserHashesPasswordBeforeSaving() {
        User newUser = new User(null, "Alice", 24, " Alice@example.com ", "9876543210", "my-secret");
        when(userRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = userService.createUser(newUser);

        assertEquals("Alice", savedUser.getName());
        assertEquals(24, savedUser.getAge());
        assertEquals("alice@example.com", savedUser.getEmail());
        assertEquals("9876543210", savedUser.getPhoneNumber());
        assertNotEquals("my-secret", savedUser.getPassword());
        assertTrue(passwordEncoder.matches("my-secret", savedUser.getPassword()));
        verify(userRepository).save(savedUser);
    }

    @Test
    void authenticateUserMatchesStoredHashedPassword() {
        User existingUser = new User(1L, "Alice", 24, "alice@example.com", "9876543210", passwordEncoder.encode("my-secret"));
        when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(existingUser));

        Optional<User> authenticatedUser = userService.authenticateUser("Alice@example.com", "my-secret");

        assertTrue(authenticatedUser.isPresent());
        assertEquals(existingUser.getId(), authenticatedUser.get().getId());
    }

    @Test
    void authenticateUserRejectsWrongPassword() {
        User existingUser = new User(1L, "Alice", 24, "alice@example.com", "9876543210", passwordEncoder.encode("my-secret"));
        when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(existingUser));

        Optional<User> authenticatedUser = userService.authenticateUser("alice@example.com", "wrong-password");

        assertFalse(authenticatedUser.isPresent());
    }

    @Test
    void authenticateUserUpgradesLegacyPlainTextPassword() {
        User legacyUser = new User(1L, "Alice", 24, "alice@example.com", "9876543210", "my-secret");
        when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(legacyUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<User> authenticatedUser = userService.authenticateUser("alice@example.com", "my-secret");

        assertTrue(authenticatedUser.isPresent());
        assertNotEquals("my-secret", legacyUser.getPassword());
        assertTrue(passwordEncoder.matches("my-secret", legacyUser.getPassword()));
        verify(userRepository).save(legacyUser);
    }
}
