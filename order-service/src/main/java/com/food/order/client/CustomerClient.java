package com.food.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.food.order.dto.CustomerResponseDTO;
import com.food.order.dto.ResponseStructure;

@FeignClient(name = "customer-service")
public interface CustomerClient {

	@GetMapping("/customer/{id}")
	ResponseStructure<CustomerResponseDTO> getCustomer(@PathVariable Integer id);

}
