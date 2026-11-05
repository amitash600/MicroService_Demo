package com.food.order.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.food.order.entity.Order;
import com.food.order.enums.Status;
import com.food.order.repository.OrderRepository;

@Repository
public class OrderDao {

	@Autowired
	private OrderRepository orderRepository;

	public Order saveOrder(Order order) {
		return orderRepository.save(order);
	}

	public List<Order> fetchAllOrders() {
		return orderRepository.findAll();
	}

	public Optional<Order> fetchOrderById(Integer orderId) {
		return orderRepository.findById(orderId);
	}

	public List<Order> fetchOrdersOfCustomer(Integer customerId) {
		return orderRepository.findByCustomerId(customerId);
	}

	public List<Order> fetchOrdersByStatus(Status status) {
		return orderRepository.findByStatus(status);
	}

	public List<Order> fetchOrdersByDate(LocalDateTime start, LocalDateTime end) {
		return orderRepository.findByOrderDateTimeBetween(start, end);
	}

	public List<Order> fetchOrdersBetweenTotalAmountRange(Double minAmount, Double maxAmount) {
		return orderRepository.findByTotalAmountBetween(minAmount, maxAmount);
	}

	public List<Order> fetchOrdersByRestaurant(Integer restaurantId) {
		return orderRepository.findDistinctByOrderItemsRestaurantId(restaurantId);
	}

	public boolean existsByCustomer(Integer customerId) {
		return orderRepository.existsByCustomerId(customerId);
	}

}
