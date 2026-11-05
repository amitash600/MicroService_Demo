package com.food.order.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.food.order.entity.Order;
import com.food.order.enums.Status;

public interface OrderRepository extends JpaRepository<Order, Integer> {

	List<Order> findByCustomerId(Integer customerId);

	List<Order> findByStatus(Status status);

	List<Order> findByOrderDateTimeBetween(LocalDateTime start, LocalDateTime end);

	List<Order> findByTotalAmountBetween(Double minAmount, Double maxAmount);

	List<Order> findDistinctByOrderItemsRestaurantId(Integer restaurantId);

	boolean existsByCustomerId(Integer customerId);

}
