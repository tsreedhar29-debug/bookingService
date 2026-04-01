package com.bms.bookingService.service;

import com.bms.bookingService.dto.BookingRequest;
import com.bms.bookingService.dto.BookingResponse;
import com.bms.bookingService.entity.Booking;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request);
}
