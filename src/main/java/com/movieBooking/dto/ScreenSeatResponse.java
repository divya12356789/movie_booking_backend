package com.movieBooking.dto;

import com.movieBooking.model.ScreenSeat;

public record ScreenSeatResponse(
        Long id,
        Long screenId,
        String seatNumber,
        String seatType,
        String rowLabel,
        Integer seatOrder) {

    public static ScreenSeatResponse from(ScreenSeat screenSeat) {
        return new ScreenSeatResponse(
                screenSeat.getId(),
                screenSeat.getScreen().getId(),
                screenSeat.getSeatNumber(),
                screenSeat.getSeatType(),
                screenSeat.getRowLabel(),
                screenSeat.getSeatOrder());
    }
}
