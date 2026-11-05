package com.food.menu.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.food.menu.entity.MenuItem;
import com.food.menu.repository.MenuItemRepository;

@Repository
public class MenuItemDao {

	@Autowired
	private MenuItemRepository menuItemRepository;

	public MenuItem saveMenuItem(MenuItem menuItem) {
		return menuItemRepository.save(menuItem);
	}

	public List<MenuItem> fetchAllMenuItems() {
		return menuItemRepository.findAll();
	}

	public List<MenuItem> fetchMenuItemsByRestaurant(Integer restaurantId) {
		return menuItemRepository.findByRestaurantId(restaurantId);
	}

	public Optional<MenuItem> fetchMenuItemById(Integer id) {
		return menuItemRepository.findById(id);
	}

	public List<MenuItem> fetchMenuItemsByPriceGreaterThan(Double price) {
		return menuItemRepository.findByPriceGreaterThan(price);
	}

	public List<MenuItem> fetchMenuItemsByName(String name) {
		return menuItemRepository.findByItemName(name);
	}

	public boolean existsByRestaurant(Integer restaurantId) {
		return menuItemRepository.existsByRestaurantId(restaurantId);
	}

	public void deleteMenuItem(Integer id) {
		menuItemRepository.deleteById(id);
	}

}
