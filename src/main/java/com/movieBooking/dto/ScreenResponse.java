package com.movieBooking.dto;

import com.movieBooking.model.Screen;

public record ScreenResponse(Long id, Long theatreId, String name, int totalSeats) {

    public static ScreenResponse from(Screen screen) {
        return new ScreenResponse(
                screen.getId(),
                screen.getTheatre().getId(),
                screen.getName(),
                screen.getTotalSeats());
    }
}
