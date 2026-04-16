package com.movieBooking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "screen_seats")
public class ScreenSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @Column(name = "seat_number", nullable = false, length = 20)
    private String seatNumber;

    @Column(name = "seat_type", nullable = false, length = 30)
    private String seatType = "REGULAR";

    @Column(name = "row_label", length = 10)
    private String rowLabel;

    @Column(name = "seat_order")
    private Integer seatOrder;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public ScreenSeat() {
    }

    public ScreenSeat(Long id, Screen screen, String seatNumber, String seatType, String rowLabel, Integer seatOrder, boolean active) {
        this.id = id;
        this.screen = screen;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.rowLabel = rowLabel;
        this.seatOrder = seatOrder;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Screen getScreen() {
        return screen;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public String getRowLabel() {
        return rowLabel;
    }

    public void setRowLabel(String rowLabel) {
        this.rowLabel = rowLabel;
    }

    public Integer getSeatOrder() {
        return seatOrder;
    }

    public void setSeatOrder(Integer seatOrder) {
        this.seatOrder = seatOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
