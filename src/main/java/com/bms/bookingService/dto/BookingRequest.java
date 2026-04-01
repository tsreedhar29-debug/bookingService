package com.bms.bookingService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotNull
    @Valid
    private UserDTO user;

    @NotNull
    @Valid
    private ShowDTO show;

    @NotNull
    @Valid
    private List<SeatDTO> seats;

    @Valid
    private OfferDTO offer;

    @NotNull
    @Valid
    private PaymentDTO payment;

    private MetadataDTO metadata;

}
