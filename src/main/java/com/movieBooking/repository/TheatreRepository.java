package com.movieBooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.movieBooking.model.Theatre;

public interface TheatreRepository extends JpaRepository<Theatre, Long> {
    List<Theatre> findByCityIdAndActiveTrueOrderByNameAsc(Long cityId);
}
