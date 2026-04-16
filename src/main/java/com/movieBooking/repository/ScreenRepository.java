package com.movieBooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.movieBooking.model.Screen;

public interface ScreenRepository extends JpaRepository<Screen, Long> {
    List<Screen> findByTheatreIdAndActiveTrueOrderByNameAsc(Long theatreId);
}
