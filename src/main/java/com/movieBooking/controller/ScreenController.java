package com.movieBooking.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.movieBooking.dto.CreateScreenRequest;
import com.movieBooking.dto.CreateScreenSeatRequest;
import com.movieBooking.dto.ScreenResponse;
import com.movieBooking.dto.ScreenSeatResponse;
import com.movieBooking.service.ScreenService;

@CrossOrigin(origins = "*")
@RestController
public class ScreenController {

    private final ScreenService screenService;

    public ScreenController(ScreenService screenService) {
        this.screenService = screenService;
    }

    @GetMapping("/screens")
    public ResponseEntity<?> getScreens(@RequestParam(required = false) Long theatreId) {
        try {
            if (theatreId == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "theatreId query parameter is required."));
            }

            List<ScreenResponse> screens = screenService.getScreens(theatreId);
            return ResponseEntity.ok(screens);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }

    @PostMapping("/screens")
    public ResponseEntity<?> createScreen(@RequestBody CreateScreenRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(screenService.createScreen(request));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }

    @GetMapping("/screen-seats")
    public ResponseEntity<?> getScreenSeats(@RequestParam(required = false) Long screenId) {
        try {
            if (screenId == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "screenId query parameter is required."));
            }

            List<ScreenSeatResponse> screenSeats = screenService.getScreenSeats(screenId);
            return ResponseEntity.ok(screenSeats);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }

    @PostMapping("/screen-seats")
    public ResponseEntity<?> createScreenSeat(@RequestBody CreateScreenSeatRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(screenService.createScreenSeat(request));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }
}
