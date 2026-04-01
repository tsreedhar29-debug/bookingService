package com.bms.bookingService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private String bookingId;
    private String status; // CONFIRMED, FAILED

    private double totalAmount;

    private List<String> seats;

    private String theatreName;
    private String movieName;

    private String showTime;

    private String message;
}
