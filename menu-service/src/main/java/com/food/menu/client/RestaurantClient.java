package com.food.menu.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.food.menu.dto.ResponseStructure;
import com.food.menu.entity.Restaurant;

@FeignClient(name = "restaurant-service")
public interface RestaurantClient {

	@GetMapping("/restaurant/{id}")
	ResponseStructure<Restaurant> getRestaurant(@PathVariable Integer id);

}
