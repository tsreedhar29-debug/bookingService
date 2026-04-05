package com.bms.bookingService.saga;


import com.bms.bookingService.dto.BookingEvent;
import com.bms.bookingService.dto.BookingEventType;
import com.bms.bookingService.entity.*;
import com.bms.bookingService.feignClients.PaymentServiceClient;
import com.bms.bookingService.feignClients.TheatreServiceClient;
import com.bms.bookingService.repository.BookingRepository;
import com.bms.bookingService.repository.BookingSagaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingSagaOrchestrator {

    private final BookingRepository bookingRepository;
    private final BookingSagaRepository sagaRepository;
    private final TheatreServiceClient theatreServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;

    @Transactional
    public void startBookingSaga(Booking booking) {
        String sagaId = UUID.randomUUID().toString();
        booking.setSagaId(sagaId);
        booking.setStatus(BookingStatus.INITIATED);
        bookingRepository.save(booking);

        BookingSaga saga = BookingSaga.builder()
            .bookingId(booking.getId())
            .status(SagaStatus.STARTED)
            .currentStep(SagaStep.BOOKING_CREATED)
            .build();
        sagaRepository.save(saga);

        log.info("Started booking saga: {} for booking: {}", sagaId, booking.getId());

        publishEvent(booking, BookingEventType.BOOKING_INITIATED);

        executeSeatsReservation(booking, saga);
    }

    @Transactional
    public void executeSeatsReservation(Booking booking, BookingSaga saga) {
        try {
            log.info("Executing seats reservation for booking: {}", booking.getId());

            saga.setCurrentStep(SagaStep.SEATS_RESERVATION);
            saga.setStatus(SagaStatus.IN_PROGRESS);
            sagaRepository.save(saga);

            List<String> seatIds = Arrays.asList(booking.getSeatIds().split(","));
            Map<String, Object> response =
                theatreServiceClient.reserveSeats(booking.getShowId(), seatIds);

            if (Boolean.TRUE.equals(response.get("success"))) {
                booking.setStatus(BookingStatus.SEATS_RESERVED);
                bookingRepository.save(booking);

                publishEvent(booking, BookingEventType.SEATS_RESERVED);

                executePaymentProcessing(booking, saga);
            } else {
                handleSeatsReservationFailure(booking, saga,
                    "Seats reservation failed: " + response.get("message"));
            }
        } catch (Exception ex) {
            log.error("Error during seats reservation: {}", ex.getMessage(), ex);
            handleSeatsReservationFailure(booking, saga, ex.getMessage());
        }
    }

    @Transactional
    public void executePaymentProcessing(Booking booking, BookingSaga saga) {
        try {
            log.info("Executing payment processing for booking: {}", booking.getId());

            saga.setCurrentStep(SagaStep.PAYMENT_PROCESSING);
            sagaRepository.save(saga);

            booking.setStatus(BookingStatus.PAYMENT_PENDING);
            bookingRepository.save(booking);

            Map<String, Object> paymentRequest = Map.of(
                "bookingId", booking.getId(),
                "userId", booking.getUserId(),
                "amount", booking.getFinalAmount(),
                "sagaId", booking.getSagaId()
            );

            publishEvent(booking, BookingEventType.PAYMENT_INITIATED);

            Map<String, Object> response =
                paymentServiceClient.initiatePayment(paymentRequest);

            if (Boolean.TRUE.equals(response.get("success"))) {
                booking.setPaymentId((String) response.get("paymentId"));
                booking.setStatus(BookingStatus.PAYMENT_COMPLETED);
                bookingRepository.save(booking);

                publishEvent(booking, BookingEventType.PAYMENT_COMPLETED);

                completeBooking(booking, saga);
            } else {
                handlePaymentFailure(booking, saga,
                    "Payment failed: " + response.get("message"));
            }
        } catch (Exception ex) {
            log.error("Error during payment processing: {}", ex.getMessage(), ex);
            handlePaymentFailure(booking, saga, ex.getMessage());
        }
    }

    @Transactional
    public void completeBooking(Booking booking, BookingSaga saga) {
        log.info("Completing booking: {}", booking.getId());

        saga.setCurrentStep(SagaStep.BOOKING_CONFIRMATION);
        saga.setStatus(SagaStatus.COMPLETED);
        sagaRepository.save(saga);

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingReference(generateBookingReference());
        bookingRepository.save(booking);

        publishEvent(booking, BookingEventType.BOOKING_CONFIRMED);

        log.info("Booking completed successfully: {}", booking.getId());
    }

    @Transactional
    public void handleSeatsReservationFailure(
            Booking booking, BookingSaga saga, String errorMessage) {
        log.error("Seats reservation failed for booking: {}, error: {}",
                  booking.getId(), errorMessage);

        saga.setStatus(SagaStatus.FAILED);
        saga.setErrorMessage(errorMessage);
        sagaRepository.save(saga);

        booking.setStatus(BookingStatus.FAILED);
        bookingRepository.save(booking);

        publishEvent(booking, BookingEventType.SEATS_RESERVATION_FAILED);
        publishEvent(booking, BookingEventType.BOOKING_FAILED);
    }

    @Transactional
    public void handlePaymentFailure(
            Booking booking, BookingSaga saga, String errorMessage) {
        log.error("Payment failed for booking: {}, error: {}",
                  booking.getId(), errorMessage);

        saga.setStatus(SagaStatus.COMPENSATING);
        saga.setErrorMessage(errorMessage);
        sagaRepository.save(saga);

        compensateSeatsReservation(booking, saga);
    }

    @Transactional
    public void compensateSeatsReservation(Booking booking, BookingSaga saga) {
        try {
            log.info("Compensating seats reservation for booking: {}", booking.getId());

            List<String> seatIds = Arrays.asList(booking.getSeatIds().split(","));
            theatreServiceClient.releaseSeats(booking.getShowId(), seatIds);

            saga.setStatus(SagaStatus.COMPENSATED);
            sagaRepository.save(saga);

            booking.setStatus(BookingStatus.FAILED);
            bookingRepository.save(booking);

            publishEvent(booking, BookingEventType.SEATS_RELEASED);
            publishEvent(booking, BookingEventType.BOOKING_FAILED);

            log.info("Compensation completed for booking: {}", booking.getId());
        } catch (Exception ex) {
            log.error("Error during compensation: {}", ex.getMessage(), ex);
            saga.setStatus(SagaStatus.FAILED);
            saga.setErrorMessage("Compensation failed: " + ex.getMessage());
            sagaRepository.save(saga);
        }
    }

    private void publishEvent(Booking booking, BookingEventType eventType) {
        List<String> seatIds = Arrays.asList(booking.getSeatIds().split(","));

        BookingEvent event = BookingEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .bookingId(booking.getId())
            .userId(booking.getUserId())
            .showId(booking.getShowId())
            .theatreId(booking.getTheatreId())
            .seatIds(seatIds)
            .amount(booking.getBaseAmount())
            .discount(booking.getDiscount())
            .finalAmount(booking.getFinalAmount())
            .eventType(eventType)
            .timestamp(LocalDateTime.now())
            .sagaId(booking.getSagaId())
            .build();

        kafkaTemplate.send("booking-events", event);
        log.info("Published event: {} for booking: {}", eventType, booking.getId());
    }

    private String generateBookingReference() {
        return "BKG-" + System.currentTimeMillis() +
               "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
