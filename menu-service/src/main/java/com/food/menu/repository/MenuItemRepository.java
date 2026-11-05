package com.food.menu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.food.menu.entity.MenuItem;

public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {

	List<MenuItem> findByRestaurantId(Integer restaurantId);

	List<MenuItem> findByPriceGreaterThan(Double price);

	List<MenuItem> findByItemName(String itemName);

	boolean existsByRestaurantId(Integer restaurantId);

}
