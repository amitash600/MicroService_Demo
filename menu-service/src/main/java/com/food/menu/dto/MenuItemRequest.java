package com.food.menu.dto;

public class MenuItemRequest {

	private String itemName;
	private Double price;
	private Boolean availability;
	// Accepts flat {"restaurantId": 1}
	private Integer restaurantId;
	// Accepts nested {"restaurant": {"restaurantId": 1}} (same shape as the monolith)
	private RestaurantRef restaurant;

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Boolean getAvailability() {
		return availability;
	}

	public void setAvailability(Boolean availability) {
		this.availability = availability;
	}

	public Integer getRestaurantId() {
		return restaurantId;
	}

	public void setRestaurantId(Integer restaurantId) {
		this.restaurantId = restaurantId;
	}

	public RestaurantRef getRestaurant() {
		return restaurant;
	}

	public void setRestaurant(RestaurantRef restaurant) {
		this.restaurant = restaurant;
	}

	public Integer resolveRestaurantId() {
		if (restaurantId != null) {
			return restaurantId;
		}
		if (restaurant != null) {
			return restaurant.getRestaurantId();
		}
		return null;
	}

	public static class RestaurantRef {
		private Integer restaurantId;

		public Integer getRestaurantId() {
			return restaurantId;
		}

		public void setRestaurantId(Integer restaurantId) {
			this.restaurantId = restaurantId;
		}
	}

}
