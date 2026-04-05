package com.bms.bookingService.controller;

import com.bms.bookingService.dto.BookingRequest;
import com.bms.bookingService.dto.BookingResponse;
import com.bms.bookingService.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/book")

    public ResponseEntity<BookingResponse> createBooking(
            @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBooking(
            @PathVariable String bookingId) {
        BookingResponse response = bookingService.getBooking(bookingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getUserBookings(
            @PathVariable String userId) {
        List<BookingResponse> bookings = bookingService.getUserBookings(userId);
        return ResponseEntity.ok(bookings);
    }

}
