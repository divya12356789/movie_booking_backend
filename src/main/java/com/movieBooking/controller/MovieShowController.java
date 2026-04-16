package com.movieBooking.controller;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.movieBooking.dto.CreateMovieShowRequest;
import com.movieBooking.dto.MovieShowResponse;
import com.movieBooking.dto.ShowSeatResponse;
import com.movieBooking.service.MovieShowService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/movie-shows")
public class MovieShowController {

    private final MovieShowService movieShowService;

    public MovieShowController(MovieShowService movieShowService) {
        this.movieShowService = movieShowService;
    }

    @GetMapping
    public ResponseEntity<?> getScheduledShows(
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Long movieId) {
        try {
            List<MovieShowResponse> shows = movieShowService.getScheduledShows(cityId, movieId);
            return ResponseEntity.ok(shows);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createMovieShow(@RequestBody CreateMovieShowRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(movieShowService.createMovieShow(request));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (NoSuchElementException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", exception.getMessage()));
        }
    }

    @GetMapping("/{showId}/seats")
    public ResponseEntity<?> getShowSeats(@PathVariable Long showId) {
        try {
            List<ShowSeatResponse> showSeats = movieShowService.getShowSeats(showId);
            return ResponseEntity.ok(showSeats);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (NoSuchElementException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", exception.getMessage()));
        }
    }
}
