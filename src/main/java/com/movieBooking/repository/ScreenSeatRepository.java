package com.movieBooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.movieBooking.model.ScreenSeat;

public interface ScreenSeatRepository extends JpaRepository<ScreenSeat, Long> {
    List<ScreenSeat> findByScreenIdAndActiveTrueOrderBySeatOrderAscIdAsc(Long screenId);

    boolean existsByScreenId(Long screenId);
}
