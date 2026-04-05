package com.bms.bookingService.entity;

public enum BookingStatus {
    INITIATED,
    SEATS_RESERVED,
    PAYMENT_PENDING,
    PAYMENT_COMPLETED,
    CONFIRMED,
    FAILED,
    CANCELLED
}
