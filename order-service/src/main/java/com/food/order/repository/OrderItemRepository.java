package com.food.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.food.order.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

	List<OrderItem> findByOrderOrderIdAndMenuItemId(Integer orderId, Integer menuItemId);

	List<OrderItem> findByOrderOrderId(Integer orderId);

	boolean existsByMenuItemId(Integer menuItemId);

}
