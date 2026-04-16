package com.movieBooking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.movieBooking.model.ShowBookingSeat;

public interface ShowBookingSeatRepository extends JpaRepository<ShowBookingSeat, Long> {

    @EntityGraph(attributePaths = {
            "booking",
            "showSeat",
            "showSeat.screenSeat",
            "showSeat.show",
            "showSeat.show.movie",
            "showSeat.show.screen",
            "showSeat.show.screen.theatre",
            "showSeat.show.screen.theatre.city"
    })
    Optional<ShowBookingSeat> findFirstByBooking_IdOrderByIdAsc(Long bookingId);

    @EntityGraph(attributePaths = {
            "booking",
            "showSeat",
            "showSeat.screenSeat",
            "showSeat.show",
            "showSeat.show.movie",
            "showSeat.show.screen",
            "showSeat.show.screen.theatre",
            "showSeat.show.screen.theatre.city"
    })
    List<ShowBookingSeat> findByBooking_IdOrderByIdAsc(Long bookingId);
}
