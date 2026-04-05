package com.bms.bookingService.repository;


import com.bms.bookingService.entity.BookingSaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingSagaRepository extends JpaRepository<BookingSaga, String> {
    Optional<BookingSaga> findByBookingId(String bookingId);
}
