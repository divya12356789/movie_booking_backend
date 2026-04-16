package com.movieBooking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.movieBooking.model.Seat;

import jakarta.persistence.LockModeType;

// Repository for performing database operations on Seat entities.
public interface SeatRepository extends JpaRepository<Seat, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select seat from Seat seat where seat.id = :id")
    Optional<Seat> findByIdForUpdate(@Param("id") Long id);
}
