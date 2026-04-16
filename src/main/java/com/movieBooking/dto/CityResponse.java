package com.movieBooking.dto;

import com.movieBooking.model.City;

public record CityResponse(Long id, String name, String state) {

    public static CityResponse from(City city) {
        return new CityResponse(city.getId(), city.getName(), city.getState());
    }
}
