package com.bms.bookingService.feignClients;

import com.bms.bookingService.dto.PaymentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PAYMENTSERVICE")
public interface PaymentClient {

    @PostMapping("/payment/pay")
    boolean pay(@RequestBody PaymentRequest request);
}