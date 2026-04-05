package com.bms.bookingService.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class DiscountService {

    private static final BigDecimal THIRD_TICKET_DISCOUNT = new BigDecimal("0.50");
    private static final BigDecimal AFTERNOON_DISCOUNT = new BigDecimal("0.20");

    public BigDecimal calculateDiscount(
            BigDecimal baseAmount,
            int numberOfTickets,
            LocalDateTime showTime) {

        BigDecimal totalDiscount = BigDecimal.ZERO;

        if (numberOfTickets >= 3) {
            BigDecimal thirdTicketDiscount =
                baseAmount.divide(new BigDecimal(numberOfTickets))
                          .multiply(THIRD_TICKET_DISCOUNT);
            totalDiscount = totalDiscount.add(thirdTicketDiscount);
            log.info("Applied 50% discount on third ticket: {}", thirdTicketDiscount);
        }

        if (isAfternoonShow(showTime)) {
            BigDecimal afternoonDiscount = baseAmount.multiply(AFTERNOON_DISCOUNT);
            totalDiscount = totalDiscount.add(afternoonDiscount);
            log.info("Applied 20% afternoon show discount: {}", afternoonDiscount);
        }

        return totalDiscount;
    }

    private boolean isAfternoonShow(LocalDateTime showTime) {
        int hour = showTime.getHour();
        return hour >= 12 && hour < 17;
    }

    public BigDecimal calculateFinalAmount(
            BigDecimal baseAmount,
            List<String> seatIds,
            LocalDateTime showTime) {

        int numberOfTickets = seatIds.size();
        BigDecimal discount = calculateDiscount(baseAmount, numberOfTickets, showTime);
        BigDecimal finalAmount = baseAmount.subtract(discount);

        log.info("Base amount: {}, Discount: {}, Final amount: {}",
                 baseAmount, discount, finalAmount);

        return finalAmount.max(BigDecimal.ZERO);
    }
}
