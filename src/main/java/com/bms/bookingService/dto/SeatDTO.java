package com.bms.bookingService.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatDTO {
    @NotBlank
    private String seatId;

    private String row;
    private String number;

    private double price;

    private String type;
}
