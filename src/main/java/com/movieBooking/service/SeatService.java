package com.movieBooking.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.movieBooking.model.Seat;
import com.movieBooking.repository.SeatRepository;

// Service class for handling seat-related operations.
@Service
public class SeatService {

    @Autowired
    private SeatRepository seatRepository;

    // Save a new seat in the database.
    public Seat createSeat(Seat seat) {
        return seatRepository.save(seat);
    }

    // Find a seat by its ID.
    public Optional<Seat> getSeatById(Long id) {
        return seatRepository.findById(id);
    }

    // Retrieve all seats from the database.
    public List<Seat> getAllSeats() {
        return seatRepository.findAll();
    }
}