package com.movieBooking.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.movieBooking.model.MovieShow;

public interface MovieShowRepository extends JpaRepository<MovieShow, Long> {

    @EntityGraph(attributePaths = { "movie", "screen", "screen.theatre", "screen.theatre.city" })
    @Query("""
            select ms
            from MovieShow ms
            where ms.screen.theatre.city.id = :cityId
              and ms.status = 'SCHEDULED'
              and ms.showStart >= :currentTime
            order by ms.movie.title asc, ms.showStart asc
            """)
    List<MovieShow> findScheduledByCityId(@Param("cityId") Long cityId, @Param("currentTime") LocalDateTime currentTime);

    @EntityGraph(attributePaths = { "movie", "screen", "screen.theatre", "screen.theatre.city" })
    @Query("""
            select ms
            from MovieShow ms
            where (:cityId is null or ms.screen.theatre.city.id = :cityId)
              and (:movieId is null or ms.movie.id = :movieId)
              and ms.status = 'SCHEDULED'
              and ms.showStart >= :currentTime
            order by ms.showStart asc
            """)
    List<MovieShow> findScheduledShows(
            @Param("cityId") Long cityId,
            @Param("movieId") Long movieId,
            @Param("currentTime") LocalDateTime currentTime);

    @EntityGraph(attributePaths = { "movie", "screen", "screen.theatre", "screen.theatre.city" })
    Optional<MovieShow> findById(Long id);
}
