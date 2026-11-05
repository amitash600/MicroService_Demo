package com.food.customer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.food.customer.dto.ResponseStructure;

@FeignClient(name = "order-service")
public interface OrderClient {

	@GetMapping("/order/exists/customer/{customerId}")
	ResponseStructure<Boolean> existsByCustomer(@PathVariable Integer customerId);

}
