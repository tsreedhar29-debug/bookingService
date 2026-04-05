package com.bms.bookingService.feignClients;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "theatreService")
public interface TheatreServiceClient {

    @CircuitBreaker(name = "theatreService", fallbackMethod = "reserveSeatsFallback")
    @PostMapping("/shows/{showId}/seats/reserve")
    Map<String, Object> reserveSeats(
        @PathVariable String showId,
        @RequestBody List<String> seatIds);

    @CircuitBreaker(name = "theatreService", fallbackMethod = "releaseSeatsFallback")
    @PostMapping("/shows/{showId}/seats/release")
    Map<String, Object> releaseSeats(
        @PathVariable String showId,
        @RequestBody List<String> seatIds);

    default Map<String, Object> reserveSeatsFallback(
            String showId, List<String> seatIds, Exception ex) {
        return Map.of(
            "success", false,
            "message", "Theatre service unavailable"
        );
    }

    default Map<String, Object> releaseSeatsFallback(
            String showId, List<String> seatIds, Exception ex) {
        return Map.of(
            "success", false,
            "message", "Theatre service unavailable"
        );
    }
}
