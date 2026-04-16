package com.movieBooking.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.movieBooking.dto.CityResponse;
import com.movieBooking.dto.CreateCityRequest;
import com.movieBooking.model.City;
import com.movieBooking.repository.CityRepository;

@Service
public class CityService {

    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public List<CityResponse> getActiveCities() {
        return cityRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(CityResponse::from)
                .toList();
    }

    public CityResponse createCity(CreateCityRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("City details are required.");
        }

        City city = new City();
        city.setName(normalizeRequiredText(request.name(), "City name is required."));
        city.setState(normalizeOptionalText(request.state()));
        city.setActive(true);

        return CityResponse.from(cityRepository.save(city));
    }

    public City getCityEntity(Long cityId) {
        return cityRepository.findById(cityId)
                .filter(City::isActive)
                .orElseThrow(() -> new NoSuchElementException("City not found."));
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
