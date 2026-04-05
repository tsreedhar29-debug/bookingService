package com.bms.bookingService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    private String userId;
    private String showId;
    private String theatreId;
    private String movieId;
    private LocalDate bookingDate;
    private List<String> seatNumbers;
    private String promoCode;
}
