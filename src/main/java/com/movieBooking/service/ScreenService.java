package com.movieBooking.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.movieBooking.dto.CreateScreenRequest;
import com.movieBooking.dto.CreateScreenSeatRequest;
import com.movieBooking.dto.ScreenResponse;
import com.movieBooking.dto.ScreenSeatResponse;
import com.movieBooking.model.Screen;
import com.movieBooking.model.ScreenSeat;
import com.movieBooking.repository.ScreenRepository;
import com.movieBooking.repository.ScreenSeatRepository;

@Service
public class ScreenService {

    private final ScreenRepository screenRepository;
    private final ScreenSeatRepository screenSeatRepository;
    private final TheatreService theatreService;

    public ScreenService(
            ScreenRepository screenRepository,
            ScreenSeatRepository screenSeatRepository,
            TheatreService theatreService) {
        this.screenRepository = screenRepository;
        this.screenSeatRepository = screenSeatRepository;
        this.theatreService = theatreService;
    }

    public ScreenResponse createScreen(CreateScreenRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Screen details are required.");
        }

        Screen screen = new Screen();
        screen.setTheatre(theatreService.getTheatreEntity(validateId(request.theatreId(), "Theatre ID is required.")));
        screen.setName(normalizeRequiredText(request.name(), "Screen name is required."));
        screen.setTotalSeats(request.totalSeats() == null ? 0 : Math.max(request.totalSeats(), 0));
        screen.setActive(true);

        return ScreenResponse.from(screenRepository.save(screen));
    }

    public List<ScreenResponse> getScreens(Long theatreId) {
        if (theatreId == null) {
            throw new IllegalArgumentException("Theatre ID is required.");
        }

        theatreService.getTheatreEntity(theatreId);

        return screenRepository.findByTheatreIdAndActiveTrueOrderByNameAsc(theatreId)
                .stream()
                .map(ScreenResponse::from)
                .toList();
    }

    public ScreenSeatResponse createScreenSeat(CreateScreenSeatRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Screen seat details are required.");
        }

        Screen screen = getScreenEntity(validateId(request.screenId(), "Screen ID is required."));
        ScreenSeat screenSeat = new ScreenSeat();
        screenSeat.setScreen(screen);
        screenSeat.setSeatNumber(normalizeRequiredText(request.seatNumber(), "Seat number is required."));
        screenSeat.setSeatType(normalizeSeatType(request.seatType()));
        screenSeat.setRowLabel(normalizeOptionalText(request.rowLabel()));
        screenSeat.setSeatOrder(request.seatOrder());
        screenSeat.setActive(true);

        ScreenSeat savedSeat = screenSeatRepository.save(screenSeat);
        updateScreenSeatCount(screen);
        return ScreenSeatResponse.from(savedSeat);
    }

    public List<ScreenSeatResponse> getScreenSeats(Long screenId) {
        if (screenId == null) {
            throw new IllegalArgumentException("Screen ID is required.");
        }

        getScreenEntity(screenId);
        return screenSeatRepository.findByScreenIdAndActiveTrueOrderBySeatOrderAscIdAsc(screenId)
                .stream()
                .map(ScreenSeatResponse::from)
                .toList();
    }

    public Screen getScreenEntity(Long screenId) {
        return screenRepository.findById(screenId)
                .filter(Screen::isActive)
                .orElseThrow(() -> new NoSuchElementException("Screen not found."));
    }

    public List<ScreenSeat> getActiveScreenSeatEntities(Long screenId) {
        return screenSeatRepository.findByScreenIdAndActiveTrueOrderBySeatOrderAscIdAsc(screenId);
    }

    public boolean hasScreenSeats(Long screenId) {
        return screenSeatRepository.existsByScreenId(screenId);
    }

    public void updateScreenSeatCount(Screen screen) {
        int totalSeats = getActiveScreenSeatEntities(screen.getId()).size();
        screen.setTotalSeats(totalSeats);
        screenRepository.save(screen);
    }

    private Long validateId(Long id, String message) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(message);
        }

        return id;
    }

    private String normalizeRequiredText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String normalizeSeatType(String seatType) {
        if (seatType == null || seatType.isBlank()) {
            return "REGULAR";
        }

        return seatType.trim().toUpperCase();
    }
}
