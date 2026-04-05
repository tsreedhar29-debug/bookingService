package com.bms.bookingService.service;

import com.bms.bookingService.dto.*;
import com.bms.bookingService.entity.Booking;
import com.bms.bookingService.entity.BookingStatus;
import com.bms.bookingService.exception.BusinessException;
import com.bms.bookingService.repository.BookingRepository;
import com.bms.bookingService.saga.BookingSagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService{


    private final BookingRepository bookingRepository;
    private final BookingSagaOrchestrator sagaOrchestrator;
    private final DiscountService discountService;

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Creating booking for user: {}, show: {}", request.getUserId(), request.getShowId());

        validateBookingRequest(request);

        BigDecimal baseAmount = calculateBaseAmount(request.getSeatNumbers().size());
        LocalDateTime showTime = LocalDateTime.now().plusDays(1);

        BigDecimal discount = discountService.calculateDiscount(
                baseAmount, request.getSeatNumbers().size(), showTime);
        BigDecimal finalAmount = baseAmount.subtract(discount);

        String seatIds = String.join(",", request.getSeatNumbers());

        Booking booking = Booking.builder()
                .userId(request.getUserId())
                .showId(request.getShowId())
                .theatreId(request.getTheatreId())
                .movieId(request.getMovieId())
                .seatIds(seatIds)
                .baseAmount(baseAmount)
                .discount(discount)
                .finalAmount(finalAmount)
                .status(BookingStatus.INITIATED)
                .build();

        booking = bookingRepository.save(booking);

        sagaOrchestrator.startBookingSaga(booking);

        return toResponse(booking);
    }

    public BookingResponse getBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("BOOKING_NOT_FOUND",
                        "Booking not found with id: " + bookingId));
        return toResponse(booking);
    }

    public List<BookingResponse> getUserBookings(String userId) {
        return bookingRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private void validateBookingRequest(BookingRequest request) {
        if (request.getSeatNumbers() == null || request.getSeatNumbers().isEmpty()) {
            throw new BusinessException("INVALID_REQUEST", "Seat numbers are required");
        }
        if (request.getSeatNumbers().size() > 10) {
            throw new BusinessException("INVALID_REQUEST",
                    "Maximum 10 seats can be booked at once");
        }
    }

    private BigDecimal calculateBaseAmount(int numberOfSeats) {
        BigDecimal pricePerSeat = new BigDecimal("250.00");
        return pricePerSeat.multiply(new BigDecimal(numberOfSeats));
    }

    private BookingResponse toResponse(Booking booking) {
        return BookingResponse.builder()
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .showId(booking.getShowId())
                .theatreId(booking.getTheatreId())
                .seatNumbers(List.of(booking.getSeatIds().split(",")))
                .baseAmount(booking.getBaseAmount())
                .discount(booking.getDiscount())
                .finalAmount(booking.getFinalAmount())
                .status(booking.getStatus().name())
                .bookingReference(booking.getBookingReference())
                .bookingTime(booking.getCreatedAt())
                .build();
    }
}
