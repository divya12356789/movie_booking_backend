package com.movieBooking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.movieBooking.model.ShowSeat;

import jakarta.persistence.LockModeType;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    @EntityGraph(attributePaths = { "screenSeat", "show", "show.movie", "show.screen", "show.screen.theatre", "show.screen.theatre.city" })
    List<ShowSeat> findByShowIdOrderByScreenSeatSeatOrderAscScreenSeatSeatNumberAsc(Long showId);

    boolean existsByShowId(Long showId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select showSeat
            from ShowSeat showSeat
            join fetch showSeat.screenSeat
            join fetch showSeat.show movieShow
            join fetch movieShow.movie
            join fetch movieShow.screen screen
            join fetch screen.theatre theatre
            join fetch theatre.city
            where showSeat.id = :id
            """)
    Optional<ShowSeat> findByIdForUpdate(@Param("id") Long id);
}
