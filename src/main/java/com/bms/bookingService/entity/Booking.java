package com.bms.bookingService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_booking_reference", columnList = "booking_reference")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "show_id", nullable = false)
    private String showId;

    @Column(name = "theatre_id", nullable = false)
    private String theatreId;

    @Column(name = "movie_id", nullable = false)
    private String movieId;

    @Column(name = "seat_ids", nullable = false)
    private String seatIds;

    @Column(nullable = false)
    private BigDecimal baseAmount;

    private BigDecimal discount;

    @Column(nullable = false)
    private BigDecimal finalAmount;

    @Column(name = "booking_reference", unique = true)
    private String bookingReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "saga_id")
    private String sagaId;

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
