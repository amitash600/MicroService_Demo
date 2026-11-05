package com.food.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.food.order.dto.MenuItemDTO;
import com.food.order.dto.ResponseStructure;

@FeignClient(name = "menu-service")
public interface MenuItemClient {

	@GetMapping("/menuItem/{id}")
	ResponseStructure<MenuItemDTO> getMenuItem(@PathVariable Integer id);

}
