package com.bms.bookingService.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowDTO {
    @NotBlank
    private String showId;

    private String movieId;
    private String movieName;

    private String theatreId;
    private String theatreName;

    private String city;

    private String showTime;  // "15:00"
    private String showDate;  // "2026-04-02"

    private String screenId;
}
