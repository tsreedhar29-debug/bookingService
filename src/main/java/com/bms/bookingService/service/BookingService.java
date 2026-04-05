package com.bms.bookingService.service;

import com.bms.bookingService.dto.BookingRequest;
import com.bms.bookingService.dto.BookingResponse;
import com.bms.bookingService.entity.Booking;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request);

    BookingResponse getBooking(String bookingId);

    List<BookingResponse> getUserBookings(String userId);
}
