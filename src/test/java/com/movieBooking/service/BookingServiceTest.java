package com.movieBooking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.movieBooking.dto.BookingResponse;
import com.movieBooking.dto.CreateBookingRequest;
import com.movieBooking.model.Booking;
import com.movieBooking.model.City;
import com.movieBooking.model.Movie;
import com.movieBooking.model.MovieShow;
import com.movieBooking.model.Screen;
import com.movieBooking.model.ScreenSeat;
import com.movieBooking.model.ShowBookingSeat;
import com.movieBooking.model.ShowSeat;
import com.movieBooking.model.Theatre;
import com.movieBooking.repository.BookingRepository;
import com.movieBooking.repository.ShowBookingSeatRepository;
import com.movieBooking.repository.ShowSeatRepository;
import com.movieBooking.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShowSeatRepository showSeatRepository;

    @Mock
    private ShowBookingSeatRepository showBookingSeatRepository;

    @Mock
    private MovieShowService movieShowService;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(
                bookingRepository,
                userRepository,
                showSeatRepository,
                showBookingSeatRepository,
                movieShowService);
    }

    @Test
    void createBookingMarksShowSeatBookedAndSavesBooking() {
        MovieShow movieShow = createMovieShow(5L);
        ShowSeat firstShowSeat = createShowSeat(7L, movieShow, "A1", "AVAILABLE", "250.00");
        ShowSeat secondShowSeat = createShowSeat(8L, movieShow, "A2", "AVAILABLE", "250.00");
        CreateBookingRequest request = new CreateBookingRequest(1L, 5L, List.of(7L, 8L));

        when(userRepository.existsById(1L)).thenReturn(true);
        when(movieShowService.getMovieShowEntity(5L)).thenReturn(movieShow);
        when(showSeatRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(firstShowSeat));
        when(showSeatRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(secondShowSeat));
        when(showSeatRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking savedBooking = invocation.getArgument(0);
            savedBooking.setId(99L);
            return savedBooking;
        });
        when(showBookingSeatRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse createdBooking = bookingService.createBooking(request);

        assertEquals(99L, createdBooking.id());
        assertEquals(7L, createdBooking.showSeatId());
        assertEquals(List.of(7L, 8L), createdBooking.showSeatIds());
        assertEquals("Dangal", createdBooking.movieTitle());
        assertEquals("Cinemagic Plaza", createdBooking.theatreName());
        assertEquals("A1", createdBooking.seatNumber());
        assertEquals(List.of("A1", "A2"), createdBooking.seatNumbers());
        assertEquals("BOOKED", firstShowSeat.getStatus());
        assertEquals("BOOKED", secondShowSeat.getStatus());
        assertNotNull(createdBooking.bookingCode());
        assertNotNull(createdBooking.bookingTime());

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        Booking savedBooking = bookingCaptor.getValue();
        assertEquals(1L, savedBooking.getUserId());
        assertEquals(5L, savedBooking.getShowId());
        assertEquals(new BigDecimal("500.00"), savedBooking.getTotalAmount());
        assertEquals("CONFIRMED", savedBooking.getStatus());

        verify(showSeatRepository).saveAll(any());
        verify(showBookingSeatRepository).saveAll(any());
    }

    @Test
    void createBookingRejectsMissingUser() {
        CreateBookingRequest request = new CreateBookingRequest(1L, 5L, List.of(7L));
        when(userRepository.existsById(1L)).thenReturn(false);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> bookingService.createBooking(request));

        assertEquals("User not found.", exception.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBookingRejectsMissingShowSeat() {
        MovieShow movieShow = createMovieShow(5L);
        CreateBookingRequest request = new CreateBookingRequest(1L, 5L, List.of(7L));

        when(userRepository.existsById(1L)).thenReturn(true);
        when(movieShowService.getMovieShowEntity(5L)).thenReturn(movieShow);
        when(showSeatRepository.findByIdForUpdate(7L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> bookingService.createBooking(request));

        assertEquals("Show seat not found.", exception.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBookingRejectsSeatFromDifferentShow() {
        MovieShow requestedShow = createMovieShow(5L);
        MovieShow actualShow = createMovieShow(8L);
        ShowSeat showSeat = createShowSeat(7L, actualShow, "A1", "AVAILABLE", "250.00");
        CreateBookingRequest request = new CreateBookingRequest(1L, 5L, List.of(7L));

        when(userRepository.existsById(1L)).thenReturn(true);
        when(movieShowService.getMovieShowEntity(5L)).thenReturn(requestedShow);
        when(showSeatRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(showSeat));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(request));

        assertEquals("Selected seat does not belong to the chosen show.", exception.getMessage());
        verify(showSeatRepository, never()).save(any(ShowSeat.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBookingRejectsAlreadyBookedSeat() {
        MovieShow movieShow = createMovieShow(5L);
        ShowSeat showSeat = createShowSeat(7L, movieShow, "A1", "BOOKED", "250.00");
        CreateBookingRequest request = new CreateBookingRequest(1L, 5L, List.of(7L));

        when(userRepository.existsById(1L)).thenReturn(true);
        when(movieShowService.getMovieShowEntity(5L)).thenReturn(movieShow);
        when(showSeatRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(showSeat));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> bookingService.createBooking(request));

        assertEquals("Selected show seat is no longer available.", exception.getMessage());
        verify(showSeatRepository, never()).save(any(ShowSeat.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void getBookingByIdMapsLinkedShowSeat() {
        MovieShow movieShow = createMovieShow(5L);
        ShowSeat showSeat = createShowSeat(7L, movieShow, "B2", "BOOKED", "310.00");
        Booking booking = new Booking();
        booking.setId(99L);
        booking.setUserId(1L);
        booking.setShowId(5L);
        booking.setBookingCode("CM-ABC12345");
        booking.setBookingTime(LocalDateTime.of(2026, 4, 16, 19, 30));
        booking.setTotalAmount(new BigDecimal("310.00"));
        booking.setStatus("CONFIRMED");

        ShowBookingSeat bookingSeat = new ShowBookingSeat();
        bookingSeat.setBooking(booking);
        bookingSeat.setShowSeat(showSeat);
        bookingSeat.setSeatPrice(new BigDecimal("310.00"));

        when(bookingRepository.findById(99L)).thenReturn(Optional.of(booking));
        when(showBookingSeatRepository.findByBooking_IdOrderByIdAsc(99L)).thenReturn(List.of(bookingSeat));

        BookingResponse response = bookingService.getBookingById(99L).orElseThrow();

        assertEquals(99L, response.id());
        assertEquals(5L, response.showId());
        assertEquals(7L, response.showSeatId());
        assertEquals(List.of(7L), response.showSeatIds());
        assertEquals("B2", response.seatNumber());
        assertEquals(List.of("B2"), response.seatNumbers());
        assertEquals("Cinemagic Plaza", response.theatreName());
        assertEquals("Screen 1", response.screenName());
        assertTrue(response.showStart().isAfter(LocalDateTime.of(2026, 4, 16, 0, 0)));
    }

    private MovieShow createMovieShow(Long showId) {
        City city = new City(1L, "Delhi", "Delhi", true);
        Theatre theatre = new Theatre(2L, city, "Cinemagic Plaza", "Main Road", true);
        Screen screen = new Screen(3L, theatre, "Screen 1", 120, true);
        Movie movie = new Movie(4L, "Dangal", "Drama", 161);

        MovieShow movieShow = new MovieShow();
        movieShow.setId(showId);
        movieShow.setMovie(movie);
        movieShow.setScreen(screen);
        movieShow.setShowStart(LocalDateTime.of(2026, 4, 16, 19, 30));
        movieShow.setShowEnd(LocalDateTime.of(2026, 4, 16, 22, 11));
        movieShow.setLanguage("Hindi");
        movieShow.setFormat("2D");
        movieShow.setBasePrice(new BigDecimal("250.00"));
        movieShow.setStatus("SCHEDULED");
        return movieShow;
    }

    private ShowSeat createShowSeat(Long showSeatId, MovieShow movieShow, String seatNumber, String status, String price) {
        ScreenSeat screenSeat = new ScreenSeat(6L, movieShow.getScreen(), seatNumber, "REGULAR", "A", 1, true);
        ShowSeat showSeat = new ShowSeat();
        showSeat.setId(showSeatId);
        showSeat.setShow(movieShow);
        showSeat.setScreenSeat(screenSeat);
        showSeat.setPrice(new BigDecimal(price));
        showSeat.setStatus(status);
        return showSeat;
    }
}
