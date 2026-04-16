package com.movieBooking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.movieBooking.dto.CityMovieResponse;
import com.movieBooking.dto.CreateMovieShowRequest;
import com.movieBooking.dto.MovieShowResponse;
import com.movieBooking.dto.ShowSeatResponse;
import com.movieBooking.model.Movie;
import com.movieBooking.model.MovieShow;
import com.movieBooking.model.Screen;
import com.movieBooking.model.ScreenSeat;
import com.movieBooking.model.Seat;
import com.movieBooking.model.ShowSeat;
import com.movieBooking.repository.MovieRepository;
import com.movieBooking.repository.MovieShowRepository;
import com.movieBooking.repository.SeatRepository;
import com.movieBooking.repository.ShowSeatRepository;

@Service
public class MovieShowService {

    private final MovieShowRepository movieShowRepository;
    private final MovieRepository movieRepository;
    private final ScreenService screenService;
    private final ShowSeatRepository showSeatRepository;
    private final SeatRepository legacySeatRepository;

    public MovieShowService(
            MovieShowRepository movieShowRepository,
            MovieRepository movieRepository,
            ScreenService screenService,
            ShowSeatRepository showSeatRepository,
            SeatRepository legacySeatRepository) {
        this.movieShowRepository = movieShowRepository;
        this.movieRepository = movieRepository;
        this.screenService = screenService;
        this.showSeatRepository = showSeatRepository;
        this.legacySeatRepository = legacySeatRepository;
    }

    @Transactional
    public MovieShowResponse createMovieShow(CreateMovieShowRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Movie show details are required.");
        }

        Movie movie = movieRepository.findById(validateId(request.movieId(), "Movie ID is required."))
                .orElseThrow(() -> new NoSuchElementException("Movie not found."));
        Screen screen = screenService.getScreenEntity(validateId(request.screenId(), "Screen ID is required."));

        MovieShow movieShow = new MovieShow();
        movieShow.setMovie(movie);
        movieShow.setScreen(screen);
        movieShow.setShowStart(validateDateTime(request.showStart(), "Show start time is required."));
        movieShow.setShowEnd(validateDateTime(request.showEnd(), "Show end time is required."));
        movieShow.setLanguage(normalizeRequiredText(request.language(), "Language is required."));
        movieShow.setFormat(normalizeOptionalText(request.format(), "2D"));
        movieShow.setBasePrice(validatePrice(request.basePrice()));
        movieShow.setStatus("SCHEDULED");

        MovieShow savedShow = movieShowRepository.save(movieShow);
        ensureScreenSeatsExist(screen);
        ensureShowSeatsExist(savedShow);
        return MovieShowResponse.from(reloadShow(savedShow.getId()));
    }

    public List<CityMovieResponse> getMoviesByCity(Long cityId) {
        if (cityId == null || cityId <= 0) {
            throw new IllegalArgumentException("City ID is required.");
        }

        List<MovieShow> shows = movieShowRepository.findScheduledByCityId(cityId, LocalDateTime.now());
        Map<Long, MovieAggregate> movieMap = new LinkedHashMap<>();

        for (MovieShow show : shows) {
            Movie movie = show.getMovie();
            MovieAggregate aggregate = movieMap.computeIfAbsent(movie.getId(), key -> new MovieAggregate(
                    movie.getId(),
                    movie.getTitle(),
                    movie.getGenre(),
                    movie.getDuration()));
            aggregate.registerShow(show);
        }

        return movieMap.values()
                .stream()
                .map(MovieAggregate::toResponse)
                .toList();
    }

    public List<MovieShowResponse> getScheduledShows(Long cityId, Long movieId) {
        return movieShowRepository.findScheduledShows(cityId, movieId, LocalDateTime.now())
                .stream()
                .map(MovieShowResponse::from)
                .toList();
    }

    @Transactional
    public List<ShowSeatResponse> getShowSeats(Long showId) {
        MovieShow movieShow = reloadShow(validateId(showId, "Show ID is required."));
        ensureShowSeatsExist(movieShow);

        return showSeatRepository.findByShowIdOrderByScreenSeatSeatOrderAscScreenSeatSeatNumberAsc(showId)
                .stream()
                .map(ShowSeatResponse::from)
                .toList();
    }

    public MovieShow getMovieShowEntity(Long showId) {
        return reloadShow(showId);
    }

    private MovieShow reloadShow(Long showId) {
        return movieShowRepository.findById(showId)
                .orElseThrow(() -> new NoSuchElementException("Movie show not found."));
    }

    private void ensureScreenSeatsExist(Screen screen) {
        if (screenService.hasScreenSeats(screen.getId())) {
            return;
        }

        List<Seat> legacySeats = legacySeatRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Seat::getSeatNumber, String.CASE_INSENSITIVE_ORDER))
                .toList();

        if (legacySeats.isEmpty()) {
            return;
        }

        int order = 1;
        for (Seat legacySeat : legacySeats) {
            screenService.createScreenSeat(new com.movieBooking.dto.CreateScreenSeatRequest(
                    screen.getId(),
                    legacySeat.getSeatNumber(),
                    "REGULAR",
                    extractRowLabel(legacySeat.getSeatNumber()),
                    order++));
        }
    }

    private void ensureShowSeatsExist(MovieShow movieShow) {
        if (showSeatRepository.existsByShowId(movieShow.getId())) {
            return;
        }

        List<ScreenSeat> screenSeats = screenService.getActiveScreenSeatEntities(movieShow.getScreen().getId());
        if (screenSeats.isEmpty()) {
            return;
        }

        List<ShowSeat> showSeats = new ArrayList<>();
        for (ScreenSeat screenSeat : screenSeats) {
            ShowSeat showSeat = new ShowSeat();
            showSeat.setShow(movieShow);
            showSeat.setScreenSeat(screenSeat);
            showSeat.setPrice(resolveSeatPrice(movieShow.getBasePrice(), screenSeat.getSeatType()));
            showSeat.setStatus("AVAILABLE");
            showSeats.add(showSeat);
        }

        showSeatRepository.saveAll(showSeats);
    }

    private BigDecimal resolveSeatPrice(BigDecimal basePrice, String seatType) {
        if (seatType == null) {
            return basePrice;
        }

        return switch (seatType.trim().toUpperCase(Locale.ROOT)) {
            case "PREMIUM" -> basePrice.add(new BigDecimal("60.00"));
            case "RECLINER" -> basePrice.add(new BigDecimal("150.00"));
            default -> basePrice;
        };
    }

    private String extractRowLabel(String seatNumber) {
        if (seatNumber == null || seatNumber.isBlank()) {
            return null;
        }

        StringBuilder row = new StringBuilder();
        for (char character : seatNumber.trim().toCharArray()) {
            if (Character.isLetter(character)) {
                row.append(Character.toUpperCase(character));
            } else {
                break;
            }
        }

        return row.isEmpty() ? null : row.toString();
    }

    private Long validateId(Long id, String message) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(message);
        }

        return id;
    }

    private LocalDateTime validateDateTime(LocalDateTime value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }

    private BigDecimal validatePrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("Base price must be greater than zero.");
        }

        return price;
    }

    private String normalizeRequiredText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }

    private String normalizeOptionalText(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value.trim().toUpperCase(Locale.ROOT);
    }

    private static final class MovieAggregate {
        private final Long id;
        private final String title;
        private final String genre;
        private final int duration;
        private LocalDateTime nextShowTime;
        private BigDecimal minPrice;
        private final Set<String> languages = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        private MovieAggregate(Long id, String title, String genre, int duration) {
            this.id = id;
            this.title = title;
            this.genre = genre;
            this.duration = duration;
        }

        private void registerShow(MovieShow movieShow) {
            if (nextShowTime == null || movieShow.getShowStart().isBefore(nextShowTime)) {
                nextShowTime = movieShow.getShowStart();
            }

            if (minPrice == null || movieShow.getBasePrice().compareTo(minPrice) < 0) {
                minPrice = movieShow.getBasePrice();
            }

            languages.add(movieShow.getLanguage());
        }

        private CityMovieResponse toResponse() {
            return new CityMovieResponse(id, title, genre, duration, nextShowTime, minPrice, List.copyOf(languages));
        }
    }
}
