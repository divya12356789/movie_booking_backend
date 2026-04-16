package com.movieBooking.dto;

import com.movieBooking.model.User;

public record UserResponse(Long id, String name, Integer age, String email, String phoneNumber, String marriageStatus) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getAge(),
                user.getEmail(),
            user.getPhoneNumber(),
            user.getMarriageStatus());
    }
}
