package com.bms.bookingService.feignClients;

import com.bms.bookingService.dto.SeatLockRequest;
import com.bms.bookingService.dto.SeatLockResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "SEATSERVICE")
public interface SeatClient {

    @PostMapping("/seats/lock")
    SeatLockResponse lockSeats(SeatLockRequest seats);

    @PostMapping("/seats/release")
    void releaseSeats(SeatLockRequest seats);
}