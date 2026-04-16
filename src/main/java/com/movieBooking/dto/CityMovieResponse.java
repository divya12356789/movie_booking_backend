package com.movieBooking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CityMovieResponse(
        Long id,
        String title,
        String genre,
        int duration,
        LocalDateTime nextShowTime,
        BigDecimal minPrice,
        List<String> languages) {
}
