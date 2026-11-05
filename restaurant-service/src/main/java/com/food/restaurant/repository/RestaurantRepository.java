package com.food.restaurant.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.food.restaurant.entity.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {

	List<Restaurant> findByLocation(String location);

	List<Restaurant> findByRestaurantName(String restaurantName);

}
