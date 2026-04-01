package com.bms.bookingService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfferDTO {
    private boolean applyThirdTicketDiscount;
    private boolean applyAfternoonDiscount;

    private String couponCode;

}
