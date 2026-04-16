package com.movieBooking.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.movieBooking.dto.CreateTheatreRequest;
import com.movieBooking.dto.TheatreResponse;
import com.movieBooking.model.Theatre;
import com.movieBooking.repository.TheatreRepository;

@Service
public class TheatreService {

    private final TheatreRepository theatreRepository;
    private final CityService cityService;

    public TheatreService(TheatreRepository theatreRepository, CityService cityService) {
        this.theatreRepository = theatreRepository;
        this.cityService = cityService;
    }

    public TheatreResponse createTheatre(CreateTheatreRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Theatre details are required.");
        }

        Theatre theatre = new Theatre();
        theatre.setCity(cityService.getCityEntity(validateId(request.cityId(), "City ID is required.")));
        theatre.setName(normalizeRequiredText(request.name(), "Theatre name is required."));
        theatre.setAddress(normalizeOptionalText(request.address()));
        theatre.setActive(true);

        return TheatreResponse.from(theatreRepository.save(theatre));
    }

    public List<TheatreResponse> getTheatres(Long cityId) {
        if (cityId == null) {
            throw new IllegalArgumentException("City ID is required.");
        }

        cityService.getCityEntity(cityId);

        return theatreRepository.findByCityIdAndActiveTrueOrderByNameAsc(cityId)
                .stream()
                .map(TheatreResponse::from)
                .toList();
    }

    public Theatre getTheatreEntity(Long theatreId) {
        return theatreRepository.findById(theatreId)
                .filter(Theatre::isActive)
                .orElseThrow(() -> new NoSuchElementException("Theatre not found."));
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
}
