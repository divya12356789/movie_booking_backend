package com.movieBooking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.movieBooking.model.MovieShow;

public record MovieShowResponse(
        Long id,
        Long movieId,
        String movieTitle,
        String genre,
        int duration,
        Long cityId,
        String cityName,
        Long theatreId,
        String theatreName,
        Long screenId,
        String screenName,
        LocalDateTime showStart,
        LocalDateTime showEnd,
        String language,
        String format,
        BigDecimal basePrice,
        String status) {

    public static MovieShowResponse from(MovieShow movieShow) {
        return new MovieShowResponse(
                movieShow.getId(),
                movieShow.getMovie().getId(),
                movieShow.getMovie().getTitle(),
                movieShow.getMovie().getGenre(),
                movieShow.getMovie().getDuration(),
                movieShow.getScreen().getTheatre().getCity().getId(),
                movieShow.getScreen().getTheatre().getCity().getName(),
                movieShow.getScreen().getTheatre().getId(),
                movieShow.getScreen().getTheatre().getName(),
                movieShow.getScreen().getId(),
                movieShow.getScreen().getName(),
                movieShow.getShowStart(),
                movieShow.getShowEnd(),
                movieShow.getLanguage(),
                movieShow.getFormat(),
                movieShow.getBasePrice(),
                movieShow.getStatus());
    }
}
