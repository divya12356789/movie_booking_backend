package com.movieBooking.config;

import java.io.IOException;
import java.util.Optional;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.movieBooking.service.AuthSessionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthTokenInterceptor implements HandlerInterceptor {

    public static final String AUTHENTICATED_USER_ID_ATTR = "authenticatedUserId";

    private final AuthSessionService authSessionService;

    public AuthTokenInterceptor(AuthSessionService authSessionService) {
        this.authSessionService = authSessionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String header = request.getHeader("Authorization");
        String token = extractBearerToken(header);
        Optional<Long> userId = authSessionService.resolveUserId(token);

        if (userId.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Please login first.\"}");
            return false;
        }

        request.setAttribute(AUTHENTICATED_USER_ID_ATTR, userId.get());
        return true;
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