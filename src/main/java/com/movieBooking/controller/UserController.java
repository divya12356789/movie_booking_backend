package com.movieBooking.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.movieBooking.config.AuthTokenInterceptor;
import com.movieBooking.dto.UpdateUserProfileRequest;
import com.movieBooking.dto.UserResponse;
import com.movieBooking.model.User;
import com.movieBooking.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

// REST controller for handling user requests.
@CrossOrigin(origins = "*")
@RestController
@RequestMapping({ "/users", "/user" })
public class UserController {

    private final UserService userService;
    private final HttpServletRequest request;

    public UserController(UserService userService, HttpServletRequest request) {
        this.userService = userService;
        this.request = request;
    }

    // Create a new user.
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(createdUser));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", exception.getMessage()));
        }
    }

    // Get a user by ID.
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(UserResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get all users.
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(UserResponse::from)
                .toList();
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateCurrentUser(@RequestBody UpdateUserProfileRequest updateRequest) {
        Object authenticatedUserId = request.getAttribute(AuthTokenInterceptor.AUTHENTICATED_USER_ID_ATTR);
        if (!(authenticatedUserId instanceof Long userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please login first."));
        }

        try {
            User updatedUser = userService.updateUserProfile(userId, updateRequest);
            return ResponseEntity.ok(UserResponse.from(updatedUser));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", exception.getMessage()));
        }
    }
}
