package com.food.menu.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import feign.FeignException;

import com.food.menu.client.OrderItemClient;
import com.food.menu.client.RestaurantClient;
import com.food.menu.dao.MenuItemDao;
import com.food.menu.dto.MenuItemRequest;
import com.food.menu.dto.ResponseStructure;
import com.food.menu.entity.MenuItem;
import com.food.menu.entity.Restaurant;
import com.food.menu.error.NoRecordFoundException;

@Service
public class MenuItemService {

	@Autowired
	private MenuItemDao menuItemDao;

	@Autowired
	private RestaurantClient restaurantClient;

	@Autowired
	private OrderItemClient orderItemClient;

	private void validateRestaurantExists(Integer restaurantId) {
		if (restaurantId == null) {
			throw new NoRecordFoundException("Restaurant not found");
		}
		try {
			ResponseStructure<Restaurant> res = restaurantClient.getRestaurant(restaurantId);
			if (res == null || res.getData() == null) {
				throw new NoRecordFoundException("Restaurant not found");
			}
		} catch (FeignException e) {
			throw new NoRecordFoundException("Restaurant not found");
		}
	}

	public ResponseEntity<ResponseStructure<MenuItem>> saveMenuItem(MenuItemRequest request) {

		Integer restaurantId = request.resolveRestaurantId();
		validateRestaurantExists(restaurantId);

		// price validation
		if (request.getPrice() != null && request.getPrice() < 0) {
			throw new DataIntegrityViolationException("Price cannot be negative");
		}

		// availability must exist but can be true or false
		if (request.getAvailability() == null) {
			throw new DataIntegrityViolationException("Availability must be specified");
		}

		MenuItem menuItem = new MenuItem();
		menuItem.setItemName(request.getItemName());
		menuItem.setPrice(request.getPrice());
		menuItem.setAvailability(request.getAvailability());
		menuItem.setRestaurantId(restaurantId);

		MenuItem saved = menuItemDao.saveMenuItem(menuItem);

		ResponseStructure<MenuItem> res = new ResponseStructure<>();

		res.setStatusCode(HttpStatus.CREATED.value());
		res.setMessage("Menu item saved successfully");
		res.setData(saved);

		return new ResponseEntity<ResponseStructure<MenuItem>>(res, HttpStatus.CREATED);
	}

	public ResponseEntity<ResponseStructure<List<MenuItem>>> fetchAllMenuItems() {

		List<MenuItem> items = menuItemDao.fetchAllMenuItems();

		ResponseStructure<List<MenuItem>> res = new ResponseStructure<>();

		if (!items.isEmpty()) {

			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Menu items fetched successfully");
			res.setData(items);

			return new ResponseEntity<ResponseStructure<List<MenuItem>>>(res, HttpStatus.OK);

		} else {

			throw new NoRecordFoundException("MenuItem table is empty");

		}
	}

	public ResponseEntity<ResponseStructure<List<MenuItem>>> fetchMenuItemsByRestaurant(Integer restaurantId) {

		List<MenuItem> items = menuItemDao.fetchMenuItemsByRestaurant(restaurantId);

		ResponseStructure<List<MenuItem>> res = new ResponseStructure<>();

		if (!items.isEmpty()) {

			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Menu items fetched successfully");
			res.setData(items);

			return new ResponseEntity<ResponseStructure<List<MenuItem>>>(res, HttpStatus.OK);

		} else {

			throw new NoRecordFoundException("No menu items found for this restaurant");

		}
	}

	public ResponseEntity<ResponseStructure<MenuItem>> fetchMenuItemById(Integer id) {

		Optional<MenuItem> optional = menuItemDao.fetchMenuItemById(id);

		ResponseStructure<MenuItem> res = new ResponseStructure<>();

		if (optional.isPresent()) {

			MenuItem item = optional.get();

			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Menu item fetched successfully");
			res.setData(item);

			return new ResponseEntity<ResponseStructure<MenuItem>>(res, HttpStatus.OK);

		} else {

			throw new NoRecordFoundException("Menu item ID not found");

		}
	}

	public ResponseEntity<ResponseStructure<MenuItem>> updateMenuItem(Integer id, MenuItemRequest request) {

		Optional<MenuItem> optional = menuItemDao.fetchMenuItemById(id);

		if (optional.isPresent()) {

			MenuItem existing = optional.get();

			// price validation
			if (request.getPrice() != null && request.getPrice() < 0) {
				throw new DataIntegrityViolationException("Price cannot be negative");
			}

			Integer restaurantId = request.resolveRestaurantId();
			validateRestaurantExists(restaurantId);

			// Prevent changing restaurant
			if (!existing.getRestaurantId().equals(restaurantId)) {
				throw new DataIntegrityViolationException("MenuItem cannot be moved to another restaurant");
			}

			existing.setItemName(request.getItemName());
			existing.setPrice(request.getPrice());
			existing.setAvailability(request.getAvailability());

			MenuItem updated = menuItemDao.saveMenuItem(existing);

			ResponseStructure<MenuItem> res = new ResponseStructure<>();

			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Menu item updated successfully");
			res.setData(updated);

			return new ResponseEntity<ResponseStructure<MenuItem>>(res, HttpStatus.OK);

		} else {
			throw new NoRecordFoundException("MenuItem ID not found");
		}
	}

	public ResponseEntity<ResponseStructure<List<MenuItem>>> fetchMenuItemsByPriceGreaterThan(Double price) {

		List<MenuItem> items = menuItemDao.fetchMenuItemsByPriceGreaterThan(price);

		ResponseStructure<List<MenuItem>> res = new ResponseStructure<>();

		if (!items.isEmpty()) {

			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Menu items fetched successfully");
			res.setData(items);

			return new ResponseEntity<ResponseStructure<List<MenuItem>>>(res, HttpStatus.OK);

		} else {

			throw new NoRecordFoundException("No menu items found with price greater than " + price);

		}
	}

	public ResponseEntity<ResponseStructure<List<MenuItem>>> fetchMenuItemsByName(String name) {

		List<MenuItem> items = menuItemDao.fetchMenuItemsByName(name);

		ResponseStructure<List<MenuItem>> res = new ResponseStructure<>();

		if (!items.isEmpty()) {

			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Menu items fetched successfully");
			res.setData(items);

			return new ResponseEntity<>(res, HttpStatus.OK);

		} else {

			throw new NoRecordFoundException("No menu items found with name: " + name);

		}
	}

	public ResponseEntity<ResponseStructure<String>> deleteMenuItem(Integer id) {

		Optional<MenuItem> optional = menuItemDao.fetchMenuItemById(id);

		if (optional.isPresent()) {

			// Check via order-service whether this item is used in any order
			try {
				ResponseStructure<Boolean> exists = orderItemClient.existsByMenuItem(id);
				if (exists != null && Boolean.TRUE.equals(exists.getData())) {
					throw new IllegalStateException("MenuItem cannot be deleted because it is used in orders");
				}
			} catch (FeignException e) {
				throw new IllegalStateException("Cannot verify menu item usage in orders right now");
			}

			menuItemDao.deleteMenuItem(id);

			ResponseStructure<String> res = new ResponseStructure<>();

			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Menu item deleted successfully");
			res.setData("Deleted");

			return new ResponseEntity<>(res, HttpStatus.OK);

		} else {
			throw new NoRecordFoundException("MenuItem ID not found");
		}
	}

	public ResponseEntity<ResponseStructure<Boolean>> existsByRestaurant(Integer restaurantId) {

		ResponseStructure<Boolean> res = new ResponseStructure<>();
		res.setStatusCode(HttpStatus.OK.value());
		res.setMessage("Menu item existence check completed");
		res.setData(menuItemDao.existsByRestaurant(restaurantId));

		return new ResponseEntity<>(res, HttpStatus.OK);
	}

}
