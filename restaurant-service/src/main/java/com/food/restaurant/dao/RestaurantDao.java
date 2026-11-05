package com.food.restaurant.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.food.restaurant.entity.Restaurant;
import com.food.restaurant.repository.RestaurantRepository;

@Repository
public class RestaurantDao {

	@Autowired
	private RestaurantRepository restaurantRepository;

	public Restaurant saveRestaurant(Restaurant restaurant) {
		return restaurantRepository.save(restaurant);
	}

	public List<Restaurant> fetchAllRestaurants() {
		return restaurantRepository.findAll();
	}

	public Optional<Restaurant> fetchRestaurantById(Integer id) {
		return restaurantRepository.findById(id);
	}

	public List<Restaurant> fetchRestaurantByLocation(String location) {
		return restaurantRepository.findByLocation(location);
	}

	public List<Restaurant> fetchRestaurantByName(String name) {
		return restaurantRepository.findByRestaurantName(name);
	}

	public Page<Restaurant> getRestaurantBySortingAndPage(Integer pN, Integer pS, String field, String order) {
		Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(field).ascending() : Sort.by(field).descending();
		Pageable pageable = PageRequest.of(pN, pS, sort);
		return restaurantRepository.findAll(pageable);
	}

	public void deleteRestaurant(Integer id) {
		restaurantRepository.deleteById(id);
	}

}
