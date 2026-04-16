package com.movieBooking.dto;

import com.movieBooking.model.Theatre;

public record TheatreResponse(Long id, Long cityId, String name, String address) {

    public static TheatreResponse from(Theatre theatre) {
        return new TheatreResponse(
                theatre.getId(),
                theatre.getCity().getId(),
                theatre.getName(),
                theatre.getAddress());
    }
}
