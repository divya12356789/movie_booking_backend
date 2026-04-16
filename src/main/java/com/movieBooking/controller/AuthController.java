package com.movieBooking.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.movieBooking.dto.LoginResponse;
import com.movieBooking.dto.LoginRequest;
import com.movieBooking.dto.UserResponse;
import com.movieBooking.service.AuthSessionService;
import com.movieBooking.service.UserService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthSessionService authSessionService;

    public AuthController(UserService userService, AuthSessionService authSessionService) {
        this.userService = userService;
        this.authSessionService = authSessionService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody com.movieBooking.model.User user) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(userService.createUser(user)));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", exception.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            return userService.authenticateUser(loginRequest.email(), loginRequest.password())
                    .<ResponseEntity<?>>map(user -> {
                        String token = authSessionService.createToken(user.getId());
                        return ResponseEntity.ok(new LoginResponse(UserResponse.from(user), token));
                    })
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("message", "Invalid email or password.")));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        boolean invalidated = authSessionService.invalidateToken(token);

        if (!invalidated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Session already expired."));
        }

        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }

        String prefix = "Bearer ";
        if (!authorizationHeader.startsWith(prefix)) {
            return null;
        }

        return authorizationHeader.substring(prefix.length()).trim();
    }
}
