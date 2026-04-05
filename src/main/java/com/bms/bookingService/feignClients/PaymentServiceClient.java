package com.bms.bookingService.feignClients;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "PAYMENTSERVICE")
public interface PaymentServiceClient {

    @CircuitBreaker(name = "paymentService", fallbackMethod = "initiatePaymentFallback")
    @PostMapping("/payments/initiate")
    Map<String, Object> initiatePayment(@RequestBody Map<String, Object> paymentRequest);

    default Map<String, Object> initiatePaymentFallback(
            Map<String, Object> paymentRequest, Exception ex) {
        return Map.of(
            "success", false,
            "message", "Payment service unavailable"
        );
    }
}
