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

import com.movieBooking.config.AuthTokenInterceptor;
import com.movieBooking.dto.BookingResponse;
import com.movieBooking.dto.CreateBookingRequest;
import com.movieBooking.service.BookingService;

import jakarta.servlet.http.HttpServletRequest;

// REST controller for handling booking requests.
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final HttpServletRequest request;

    public BookingController(BookingService bookingService, HttpServletRequest request) {
        this.bookingService = bookingService;
        this.request = request;
    }

    // Create a new booking.
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody CreateBookingRequest requestBody) {
        try {
            Object authenticatedUserId = request.getAttribute(AuthTokenInterceptor.AUTHENTICATED_USER_ID_ATTR);
            if (!(authenticatedUserId instanceof Long userId)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please login first."));
            }

            if (requestBody.userId() == null || !userId.equals(requestBody.userId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "You can only create bookings for your own account."));
            }

            BookingResponse createdBooking = bookingService.createBooking(requestBody);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (NoSuchElementException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", exception.getMessage()));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", exception.getMessage()));
        }
    }

    // Get a booking by ID.
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get all bookings.
    @GetMapping
    public List<BookingResponse> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyBookings() {
        Object authenticatedUserId = request.getAttribute(AuthTokenInterceptor.AUTHENTICATED_USER_ID_ATTR);
        if (!(authenticatedUserId instanceof Long userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please login first."));
        }

        return ResponseEntity.ok(bookingService.getBookingsForUser(userId));
    }
}
