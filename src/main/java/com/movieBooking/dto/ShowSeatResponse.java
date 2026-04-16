package com.movieBooking.dto;

import java.math.BigDecimal;

import com.movieBooking.model.ShowSeat;

public record ShowSeatResponse(
        Long id,
        Long showId,
        Long screenSeatId,
        String seatNumber,
        String seatType,
        String rowLabel,
        Integer seatOrder,
        BigDecimal price,
        String status) {

    public static ShowSeatResponse from(ShowSeat showSeat) {
        return new ShowSeatResponse(
                showSeat.getId(),
                showSeat.getShow().getId(),
                showSeat.getScreenSeat().getId(),
                showSeat.getScreenSeat().getSeatNumber(),
                showSeat.getScreenSeat().getSeatType(),
                showSeat.getScreenSeat().getRowLabel(),
                showSeat.getScreenSeat().getSeatOrder(),
                showSeat.getPrice(),
                showSeat.getStatus());
    }
}
