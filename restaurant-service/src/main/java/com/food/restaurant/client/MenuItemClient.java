package com.food.restaurant.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.food.restaurant.dto.ResponseStructure;

@FeignClient(name = "menu-service")
public interface MenuItemClient {

	@GetMapping("/menuItem/exists/restaurant/{restaurantId}")
	ResponseStructure<Boolean> existsByRestaurant(@PathVariable Integer restaurantId);

}
