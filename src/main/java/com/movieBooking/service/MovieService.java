package com.movieBooking.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.movieBooking.model.Movie;
import com.movieBooking.repository.MovieRepository;

// Service class for handling movie-related operations.
@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    // Save a new movie in the database.
    public Movie createMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    // Find a movie by its ID.
    public Optional<Movie> getMovieById(Long id) {
        return movieRepository.findById(id);
    }

    // Retrieve all movies from the database.
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }
}