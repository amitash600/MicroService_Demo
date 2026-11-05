package com.food.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.food.order.dto.PaymentRequest;
import com.food.order.dto.PaymentResponseDTO;
import com.food.order.dto.ResponseStructure;

@FeignClient(name = "payment-service")
public interface PaymentClient {

	@PostMapping("/payment")
	ResponseStructure<PaymentResponseDTO> createPayment(@RequestBody PaymentRequest request);

	@GetMapping("/payment/order/{orderId}")
	ResponseStructure<PaymentResponseDTO> getPaymentByOrder(@PathVariable Integer orderId);

}
