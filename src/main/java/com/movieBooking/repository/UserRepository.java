package com.movieBooking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.movieBooking.model.User;

// Repository for performing database operations on User entities.
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
