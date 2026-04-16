package com.movieBooking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateMovieShowRequest(
        Long movieId,
        Long screenId,
        LocalDateTime showStart,
        LocalDateTime showEnd,
        String language,
        String format,
        BigDecimal basePrice) {
}
