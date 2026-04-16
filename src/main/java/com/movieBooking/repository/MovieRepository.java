package com.movieBooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.movieBooking.model.Movie;

// Repository for performing database operations on Movie entities.
public interface MovieRepository extends JpaRepository<Movie, Long> {
}
