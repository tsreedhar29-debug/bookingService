package com.bms.bookingService.entity;




public enum BookingStatus {
    INITIATED,
    SEATS_LOCKED,
    PAYMENT_PENDING,
    CONFIRMED,
    FAILED,
    PAYMENT_FAILED
}