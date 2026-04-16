package com.movieBooking.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// Marks this class as a JPA entity.
@Entity
public class Seat {

    // Primary key for each seat record.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String seatNumber;
    private boolean isBooked;

    // Default constructor required by JPA.
    public Seat() {
    }

    public Seat(Long id, String seatNumber, boolean isBooked) {
        this.id = id;
        this.seatNumber = seatNumber;
        this.isBooked = isBooked;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void setBooked(boolean booked) {
        isBooked = booked;
    }
}
