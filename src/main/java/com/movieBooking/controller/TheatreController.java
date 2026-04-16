package com.movieBooking.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.movieBooking.dto.CreateTheatreRequest;
import com.movieBooking.dto.TheatreResponse;
import com.movieBooking.service.TheatreService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/theatres")
public class TheatreController {

    private final TheatreService theatreService;

    public TheatreController(TheatreService theatreService) {
        this.theatreService = theatreService;
    }

    @GetMapping
    public ResponseEntity<?> getTheatres(@RequestParam(required = false) Long cityId) {
        try {
            if (cityId == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "cityId query parameter is required."));
            }

            List<TheatreResponse> theatres = theatreService.getTheatres(cityId);
            return ResponseEntity.ok(theatres);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createTheatre(@RequestBody CreateTheatreRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(theatreService.createTheatre(request));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }
}
