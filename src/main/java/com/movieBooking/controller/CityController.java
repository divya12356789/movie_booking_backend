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
import org.springframework.web.bind.annotation.RestController;

import com.movieBooking.dto.CityMovieResponse;
import com.movieBooking.dto.CityResponse;
import com.movieBooking.dto.CreateCityRequest;
import com.movieBooking.service.CityService;
import com.movieBooking.service.MovieShowService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/cities")
public class CityController {

    private final CityService cityService;
    private final MovieShowService movieShowService;

    public CityController(CityService cityService, MovieShowService movieShowService) {
        this.cityService = cityService;
        this.movieShowService = movieShowService;
    }

    @GetMapping
    public List<CityResponse> getCities() {
        return cityService.getActiveCities();
    }

    @PostMapping
    public ResponseEntity<?> createCity(@RequestBody CreateCityRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(cityService.createCity(request));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }

    @GetMapping("/{cityId}/movies")
    public ResponseEntity<?> getMoviesByCity(@PathVariable Long cityId) {
        try {
            List<CityMovieResponse> movies = movieShowService.getMoviesByCity(cityId);
            return ResponseEntity.ok(movies);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (NoSuchElementException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", exception.getMessage()));
        }
    }
}
