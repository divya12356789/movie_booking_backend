package com.movieBooking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.movieBooking.dto.BookingResponse;
import com.movieBooking.dto.CreateBookingRequest;
import com.movieBooking.model.Booking;
import com.movieBooking.model.MovieShow;
import com.movieBooking.model.ShowBookingSeat;
import com.movieBooking.model.ShowSeat;
import com.movieBooking.repository.BookingRepository;
import com.movieBooking.repository.ShowBookingSeatRepository;
import com.movieBooking.repository.ShowSeatRepository;
import com.movieBooking.repository.UserRepository;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ShowSeatRepository showSeatRepository;
    private final ShowBookingSeatRepository showBookingSeatRepository;
    private final MovieShowService movieShowService;

    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            ShowSeatRepository showSeatRepository,
            ShowBookingSeatRepository showBookingSeatRepository,
            MovieShowService movieShowService) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.showSeatRepository = showSeatRepository;
        this.showBookingSeatRepository = showBookingSeatRepository;
        this.movieShowService = movieShowService;
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Booking details are required.");
        }

        Long userId = validateId(request.userId(), "User ID is required.");
        Long showId = validateId(request.showId(), "Show ID is required.");
        List<Long> showSeatIds = normalizeSeatIds(request.showSeatIds());

        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("User not found.");
        }

        MovieShow movieShow = movieShowService.getMovieShowEntity(showId);
        List<ShowSeat> selectedShowSeats = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Long showSeatId : showSeatIds) {
            ShowSeat showSeat = showSeatRepository.findByIdForUpdate(showSeatId)
                    .orElseThrow(() -> new NoSuchElementException("Show seat not found."));

            if (!showId.equals(showSeat.getShow().getId())) {
                throw new IllegalArgumentException("Selected seat does not belong to the chosen show.");
            }

            if (!"AVAILABLE".equalsIgnoreCase(showSeat.getStatus())) {
                throw new IllegalStateException("Selected show seat is no longer available.");
            }

            selectedShowSeats.add(showSeat);
            totalAmount = totalAmount.add(resolveBookingAmount(showSeat));
        }

        selectedShowSeats.forEach(showSeat -> {
            showSeat.setStatus("BOOKED");
            showSeat.setLockedUntil(null);
        });
        showSeatRepository.saveAll(selectedShowSeats);

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setShowId(showId);
        booking.setBookingCode(generateBookingCode());
        booking.setBookingTime(LocalDateTime.now());
        booking.setTotalAmount(totalAmount);
        booking.setStatus("CONFIRMED");

        Booking savedBooking = bookingRepository.save(booking);

        List<ShowBookingSeat> bookingSeats = new ArrayList<>();
        for (ShowSeat showSeat : selectedShowSeats) {
            ShowBookingSeat bookingSeat = new ShowBookingSeat();
            bookingSeat.setBooking(savedBooking);
            bookingSeat.setShowSeat(showSeat);
            bookingSeat.setSeatPrice(showSeat.getPrice());
            bookingSeats.add(bookingSeat);
        }
        showBookingSeatRepository.saveAll(bookingSeats);

        return toBookingResponse(savedBooking, movieShow, selectedShowSeats);
    }

    public Optional<BookingResponse> getBookingById(Long id) {
        return bookingRepository.findById(id)
                .map(this::mapBookingResponse);
    }

    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(this::mapBookingResponse)
                .toList();
    }

    public List<BookingResponse> getBookingsForUser(Long userId) {
        Long validatedUserId = validateId(userId, "User ID is required.");

        return bookingRepository.findByUserIdOrderByBookingTimeDesc(validatedUserId)
                .stream()
                .map(this::mapBookingResponse)
                .toList();
    }

    private BookingResponse mapBookingResponse(Booking booking) {
        List<ShowBookingSeat> bookingSeats = showBookingSeatRepository.findByBooking_IdOrderByIdAsc(booking.getId());
        if (bookingSeats.isEmpty()) {
            throw new NoSuchElementException("Booked show seat not found.");
        }

        List<ShowSeat> showSeats = bookingSeats.stream()
                .map(ShowBookingSeat::getShowSeat)
                .toList();
        ShowSeat showSeat = showSeats.get(0);
        MovieShow movieShow = showSeat.getShow();

        return toBookingResponse(booking, movieShow, showSeats);
    }

    private BookingResponse toBookingResponse(Booking booking, MovieShow movieShow, List<ShowSeat> showSeats) {
        ShowSeat primarySeat = showSeats.get(0);
        return new BookingResponse(
                booking.getId(),
                booking.getUserId(),
                booking.getShowId(),
                primarySeat.getId(),
                showSeats.stream().map(ShowSeat::getId).toList(),
                booking.getBookingCode(),
                booking.getBookingTime(),
                booking.getTotalAmount(),
                booking.getStatus(),
                movieShow.getMovie().getTitle(),
                movieShow.getScreen().getTheatre().getName(),
                movieShow.getScreen().getName(),
                primarySeat.getScreenSeat().getSeatNumber(),
                showSeats.stream().map(showSeat -> showSeat.getScreenSeat().getSeatNumber()).toList(),
                movieShow.getShowStart());
    }

    private BigDecimal resolveBookingAmount(ShowSeat showSeat) {
        return showSeat.getPrice() == null ? BigDecimal.ZERO : showSeat.getPrice();
    }

    private String generateBookingCode() {
        return "CM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Long validateId(Long id, String message) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(message);
        }

        return id;
    }

    private List<Long> normalizeSeatIds(List<Long> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("At least one show seat is required.");
        }

        return new ArrayList<>(new LinkedHashSet<>(seatIds.stream()
                .map(id -> validateId(id, "Show seat ID is required."))
                .toList()));
    }
}
