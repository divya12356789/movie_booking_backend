package com.movieBooking.service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

@Service
public class AuthSessionService {

    private final ConcurrentMap<String, Long> tokenToUser = new ConcurrentHashMap<>();

    public String createToken(Long userId) {
        String token = UUID.randomUUID().toString();
        tokenToUser.put(token, userId);
        return token;
    }

    public Optional<Long> resolveUserId(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(tokenToUser.get(token));
    }

    public boolean invalidateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return tokenToUser.remove(token) != null;
    }
}