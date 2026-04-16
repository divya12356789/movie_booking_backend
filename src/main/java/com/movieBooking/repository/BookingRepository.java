package com.movieBooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.movieBooking.model.Booking;

// Repository for performing database operations on Booking entities.
public interface BookingRepository extends JpaRepository<Booking, Long> {
	List<Booking> findByUserIdOrderByBookingTimeDesc(Long userId);
}
