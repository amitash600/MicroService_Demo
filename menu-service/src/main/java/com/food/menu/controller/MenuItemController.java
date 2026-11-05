package com.food.menu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.food.menu.dto.MenuItemRequest;
import com.food.menu.dto.ResponseStructure;
import com.food.menu.entity.MenuItem;
import com.food.menu.service.MenuItemService;

@RestController
@RequestMapping("/menuItem")
public class MenuItemController {

	@Autowired
	private MenuItemService menuItemService;

	@PostMapping
	public ResponseEntity<ResponseStructure<MenuItem>> saveMenuItem(@RequestBody MenuItemRequest request) {

		return menuItemService.saveMenuItem(request);
	}

	@GetMapping
	public ResponseEntity<ResponseStructure<List<MenuItem>>> fetchAllMenuItems() {
		return menuItemService.fetchAllMenuItems();
	}

	@GetMapping("/restaurant/{restaurantId}")
	public ResponseEntity<ResponseStructure<List<MenuItem>>> fetchMenuItemsByRestaurant(
			@PathVariable Integer restaurantId) {

		return menuItemService.fetchMenuItemsByRestaurant(restaurantId);
	}

	@GetMapping("/exists/restaurant/{restaurantId}")
	public ResponseEntity<ResponseStructure<Boolean>> existsByRestaurant(@PathVariable Integer restaurantId) {

		return menuItemService.existsByRestaurant(restaurantId);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ResponseStructure<MenuItem>> fetchMenuItemById(@PathVariable Integer id) {

		return menuItemService.fetchMenuItemById(id);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ResponseStructure<MenuItem>> updateMenuItem(@PathVariable Integer id,
			@RequestBody MenuItemRequest request) {

		return menuItemService.updateMenuItem(id, request);
	}

	@GetMapping("/priceMoreThan/{price}")
	public ResponseEntity<ResponseStructure<List<MenuItem>>> fetchMenuItemsByPriceGreaterThan(
			@PathVariable Double price) {

		return menuItemService.fetchMenuItemsByPriceGreaterThan(price);
	}

	@GetMapping("/name/{name}")
	public ResponseEntity<ResponseStructure<List<MenuItem>>> fetchMenuItemsByName(@PathVariable String name) {

		return menuItemService.fetchMenuItemsByName(name);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ResponseStructure<String>> deleteMenuItem(@PathVariable Integer id) {

		return menuItemService.deleteMenuItem(id);
	}
}
