package com.food.restaurant.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.food.restaurant.client.MenuItemClient;
import com.food.restaurant.dao.RestaurantDao;
import com.food.restaurant.dto.ResponseStructure;
import com.food.restaurant.entity.Restaurant;
import com.food.restaurant.error.NoRecordFoundException;

@Service
public class RestaurantService {

	@Autowired
	private RestaurantDao restaurantDao;

	@Autowired
	private MenuItemClient menuItemClient;

	public ResponseEntity<ResponseStructure<Restaurant>> saveRestaurant(Restaurant restaurant) {

		Restaurant r = restaurantDao.saveRestaurant(restaurant);

		ResponseStructure<Restaurant> res = new ResponseStructure<>();

		res.setStatusCode(HttpStatus.CREATED.value());
		res.setMessage("Restaurant saved successfully");
		res.setData(r);

		return new ResponseEntity<ResponseStructure<Restaurant>>(res, HttpStatus.CREATED);
	}

	public ResponseEntity<ResponseStructure<List<Restaurant>>> fetchAllRestaurants() {

		List<Restaurant> restaurants = restaurantDao.fetchAllRestaurants();

		ResponseStructure<List<Restaurant>> res = new ResponseStructure<>();

		if (!restaurants.isEmpty()) {

			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Restaurants fetched successfully");
			res.setData(restaurants);

			return new ResponseEntity<ResponseStructure<List<Restaurant>>>(res, HttpStatus.OK);

		} else {

			throw new NoRecordFoundException("Restaurant table is empty");

		}
	}

	public ResponseEntity<ResponseStructure<Restaurant>> fetchRestaurantById(Integer id) {

		Optional<Restaurant> optional = restaurantDao.fetchRestaurantById(id);

		ResponseStructure<Restaurant> res = new ResponseStructure<>();

		if (optional.isPresent()) {

			Restaurant restaurant = optional.get();

			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Restaurant fetched successfully");
			res.setData(restaurant);

			return new ResponseEntity<ResponseStructure<Restaurant>>(res, HttpStatus.OK);

		} else {

			throw new NoRecordFoundException("Restaurant ID not found");

		}
	}

	public ResponseEntity<ResponseStructure<Restaurant>> updateRestaurant(Integer id, Restaurant restaurant) {

		Optional<Restaurant> optional = restaurantDao.fetchRestaurantById(id);

		if (optional.isPresent()) {

			Restaurant existing = optional.get();

			existing.setRestaurantName(restaurant.getRestaurantName());
			existing.setLocation(restaurant.getLocation());

			Restaurant updated = restaurantDao.saveRestaurant(existing);

			ResponseStructure<Restaurant> res = new ResponseStructure<>();

			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Restaurant updated successfully");
			res.setData(updated);

			return new ResponseEntity<ResponseStructure<Restaurant>>(res, HttpStatus.OK);

		} else {

			throw new NoRecordFoundException("Restaurant ID not found");

		}

	}

	public ResponseEntity<ResponseStructure<String>> deleteRestaurant(Integer id) {

		Optional<Restaurant> optional = restaurantDao.fetchRestaurantById(id);

		if (optional.isPresent()) {

			// Check via menu-service whether this restaurant still has menu items
			ResponseStructure<Boolean> exists = menuItemClient.existsByRestaurant(id);
			if (exists != null && Boolean.TRUE.equals(exists.getData())) {
				throw new IllegalStateException("Restaurant cannot be deleted because it has menu items");
			}

			restaurantDao.deleteRestaurant(id);

			ResponseStructure<String> res = new ResponseStructure<>();

			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Restaurant deleted successfully");
			res.setData("Deleted");

			return new ResponseEntity<ResponseStructure<String>>(res, HttpStatus.OK);

		} else {

			throw new NoRecordFoundException("Restaurant ID not found");

		}
	}

	public ResponseEntity<ResponseStructure<List<Restaurant>>> fetchRestaurantByLocation(String location) {

		List<Restaurant> restaurants = restaurantDao.fetchRestaurantByLocation(location);

		ResponseStructure<List<Restaurant>> res = new ResponseStructure<>();

		if (!restaurants.isEmpty()) {

			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Restaurants fetched successfully");
			res.setData(restaurants);

			return new ResponseEntity<ResponseStructure<List<Restaurant>>>(res, HttpStatus.OK);

		} else {

			throw new NoRecordFoundException("No restaurants found in this location");

		}
	}

	public ResponseEntity<ResponseStructure<List<Restaurant>>> fetchRestaurantByName(String name) {

		List<Restaurant> restaurants = restaurantDao.fetchRestaurantByName(name);

		ResponseStructure<List<Restaurant>> res = new ResponseStructure<>();

		if (!restaurants.isEmpty()) {

			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Restaurants fetched successfully");
			res.setData(restaurants);

			return new ResponseEntity<ResponseStructure<List<Restaurant>>>(res, HttpStatus.OK);

		} else {

			throw new NoRecordFoundException("No restaurant found with this name");

		}
	}

	public ResponseEntity<ResponseStructure<Page<Restaurant>>> getRestaurantBySortingAndPage(Integer pN, Integer pS,
			String field, String order) {

		Page<Restaurant> page = restaurantDao.getRestaurantBySortingAndPage(pN, pS, field, order);

		ResponseStructure<Page<Restaurant>> res = new ResponseStructure<>();

		if (!page.isEmpty()) {

			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Restaurants fetched successfully");
			res.setData(page);

			return new ResponseEntity<>(res, HttpStatus.OK);

		} else {
			throw new NoRecordFoundException("No restaurants found");
		}
	}

}
