package com.movieBooking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(
        Long id,
        Long userId,
        Long showId,
        Long showSeatId,
        List<Long> showSeatIds,
        String bookingCode,
        LocalDateTime bookingTime,
        BigDecimal totalAmount,
        String status,
        String movieTitle,
        String theatreName,
        String screenName,
        String seatNumber,
        List<String> seatNumbers,
        LocalDateTime showStart) {
}
