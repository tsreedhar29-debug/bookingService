package com.bms.bookingService.service;

import com.bms.bookingService.dto.*;
import com.bms.bookingService.entity.Booking;
import com.bms.bookingService.entity.BookingStatus;
import com.bms.bookingService.feignClients.PaymentClient;
import com.bms.bookingService.feignClients.SeatClient;
import com.bms.bookingService.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingServiceImpl implements BookingService{

    @Autowired
    private SeatClient seatClient;
    @Autowired
    private PaymentClient paymentClient;
    @Autowired
    private BookingRepository repo;
 /*   @Autowired
    private KafkaTemplate<String, Object> kafka;
*/
 @Override
 public BookingResponse createBooking(BookingRequest req) {

     Booking booking = new Booking();
     booking.setId(UUID.randomUUID().toString());
     booking.setUserId(req.getUser().getUserId());
     booking.setShowId(req.getShow().getShowId());
     booking.setStatus(BookingStatus.INITIATED);
     booking.setCreatedAt(LocalDateTime.now());
        repo.save(booking);

     SeatLockResponse locked = seatClient.lockSeats(new SeatLockRequest(req.getShow().getShowId(),
             req.getSeats().stream().map(SeatDTO::getNumber).toList()));

     if (!locked.isSuccess()) {
         booking.setStatus(BookingStatus.FAILED);
         repo.save(booking);
         throw new RuntimeException("Seats not available");
     }

     booking.setStatus(BookingStatus.SEATS_LOCKED);
     repo.save(booking);

     // STEP 2: Calculate amount
     double amount = calculateAmount(req);

     // STEP 3: Payment
     boolean paid = paymentClient.pay(
             new PaymentRequest(booking.getId(), amount)
     );

     if (!paid) {
         seatClient.releaseSeats(
                 new SeatLockRequest(req.getShow().getShowId(),req.getSeats().stream().map(SeatDTO::getNumber).toList())
         );
         booking.setStatus(BookingStatus.PAYMENT_FAILED);
         repo.save(booking);
         throw new RuntimeException("Payment failed");
     }

     // SUCCESS
     booking.setStatus(BookingStatus.CONFIRMED);
     booking.setTotalAmount(amount);
     repo.save(booking);

     // Publish event
 //    kafka.send("booking.created", booking);

     return mapToResponse(booking, req);
    }
    private double calculateAmount(BookingRequest req) {

        double total = req.getSeats().stream()
                .mapToDouble(SeatDTO::getPrice)
                .sum();

        // 50% on 3rd ticket
        if (req.getSeats().size() >= 3) {
            total -= req.getSeats().get(2).getPrice() * 0.5;
        }

        // Afternoon discount
        if (req.getShow().getShowTime().compareTo("12:00") >= 0 &&
                req.getShow().getShowTime().compareTo("16:00") <= 0) {
            total *= 0.8;
        }

        return total;
    }

    private BookingResponse mapToResponse(Booking booking, BookingRequest req) {

        BookingResponse response = new BookingResponse();

        // Booking details
        response.setBookingId(booking.getId());
        response.setStatus(booking.getStatus().name());
        response.setTotalAmount(booking.getTotalAmount());

        // Seat details
        List<String> seatIds = req.getSeats()
                .stream()
                .map(SeatDTO::getSeatId)
                .toList();
        response.setSeats(seatIds);

        // Show details
        if (req.getShow() != null) {
            response.setTheatreName(req.getShow().getTheatreName());
            response.setMovieName(req.getShow().getMovieName());
            response.setShowTime(req.getShow().getShowTime());
        }

        // Message handling
        switch (booking.getStatus()) {
            case CONFIRMED -> response.setMessage("Booking confirmed successfully");
            case PAYMENT_FAILED -> response.setMessage("Payment failed. Seats released.");
            case FAILED -> response.setMessage("Booking failed. Please try again.");
            case SEATS_LOCKED -> response.setMessage("Seats locked. Awaiting payment.");
            default -> response.setMessage("Booking initiated");
        }

        return response;
    }
}
