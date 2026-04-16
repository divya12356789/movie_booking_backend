package com.movieBooking.dto;

public record UpdateUserProfileRequest(
        String name,
        Integer age,
        String email,
        String phoneNumber,
        String marriageStatus) {
}
