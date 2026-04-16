package com.movieBooking.dto;

public record CreateScreenRequest(Long theatreId, String name, Integer totalSeats) {
}
