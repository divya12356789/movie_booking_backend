package com.movieBooking.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.movieBooking.dto.UserResponse;

class UserJsonTest {

    @Test
    void userResponseMapsOnlySafeFields() {
        User user = new User(1L, "Alice", 24, "alice@example.com", "9876543210", "Single", "hashed-password");

        UserResponse userResponse = UserResponse.from(user);

        assertEquals(user.getId(), userResponse.id());
        assertEquals(user.getName(), userResponse.name());
        assertEquals(user.getAge(), userResponse.age());
        assertEquals(user.getEmail(), userResponse.email());
        assertEquals(user.getPhoneNumber(), userResponse.phoneNumber());
        assertEquals(user.getMarriageStatus(), userResponse.marriageStatus());
    }
}
