package com.movieBooking.dto;

public record LoginResponse(UserResponse user, String token) {
}