package com.movieBooking.dto;

public record CreateScreenSeatRequest(Long screenId, String seatNumber, String seatType, String rowLabel, Integer seatOrder) {
}
