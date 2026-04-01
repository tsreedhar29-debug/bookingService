package com.bms.bookingService.controller;

import com.bms.bookingService.dto.BookingRequest;
import com.bms.bookingService.dto.BookingResponse;
import com.bms.bookingService.entity.Booking;
import com.bms.bookingService.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingService service;

    @PostMapping("/book")
    public BookingResponse create(@RequestBody  @Valid BookingRequest request) {
        return service.createBooking(request);
    }

}
