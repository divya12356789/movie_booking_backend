package com.movieBooking.dto;

import java.util.List;

public record CreateBookingRequest(Long userId, Long showId, List<Long> showSeatIds) {
}
