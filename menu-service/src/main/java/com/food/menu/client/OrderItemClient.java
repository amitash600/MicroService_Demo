package com.food.menu.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.food.menu.dto.ResponseStructure;

@FeignClient(name = "order-service")
public interface OrderItemClient {

	@GetMapping("/orderItem/exists/menuItem/{menuItemId}")
	ResponseStructure<Boolean> existsByMenuItem(@PathVariable Integer menuItemId);

}
