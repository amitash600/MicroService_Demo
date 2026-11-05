package com.food.order.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.food.order.dto.OrderResponseDTO;
import com.food.order.dto.OrderStatusUpdateRequestDTO;
import com.food.order.dto.PlaceOrderRequest;
import com.food.order.dto.ResponseStructure;
import com.food.order.enums.Status;
import com.food.order.service.OrderService;

@RestController
@RequestMapping("/order")
public class OrderController {

	@Autowired
	private OrderService orderService;

	@PostMapping
	public ResponseEntity<ResponseStructure<OrderResponseDTO>> placeOrder(@RequestBody PlaceOrderRequest request) {
		return orderService.placeOrder(request);
	}

	@GetMapping("/all")
	public ResponseEntity<ResponseStructure<List<OrderResponseDTO>>> fetchAllOrders() {
		return orderService.fetchAllOrders();
	}

	@GetMapping("/exists/customer/{customerId}")
	public ResponseEntity<ResponseStructure<Boolean>> existsByCustomer(@PathVariable Integer customerId) {
		return orderService.existsByCustomer(customerId);
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<ResponseStructure<OrderResponseDTO>> fetchOrderById(@PathVariable Integer orderId) {
		return orderService.fetchOrderById(orderId);
	}

	@GetMapping("/customer/{customerId}")
	public ResponseEntity<ResponseStructure<List<OrderResponseDTO>>> fetchOrdersOfCustomer(
			@PathVariable Integer customerId) {
		return orderService.fetchOrdersOfCustomer(customerId);
	}

	@GetMapping("/status/{status}")
	public ResponseEntity<ResponseStructure<List<OrderResponseDTO>>> fetchOrdersByStatus(@PathVariable Status status) {
		return orderService.fetchOrdersByStatus(status);
	}

	@GetMapping("/by-date/{date}")
	public ResponseEntity<ResponseStructure<List<OrderResponseDTO>>> fetchOrdersByDate(
			@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

		return orderService.fetchOrdersByDate(date);
	}

	@GetMapping("/amount/{minAmount}/{maxAmount}")
	public ResponseEntity<ResponseStructure<List<OrderResponseDTO>>> getOrdersBetweenTotalAmountRange(
			@PathVariable Double minAmount, @PathVariable Double maxAmount) {

		return orderService.fetchOrdersBetweenTotalAmountRange(minAmount, maxAmount);
	}

	@GetMapping("/restaurant/{restaurantId}")
	public ResponseEntity<ResponseStructure<List<OrderResponseDTO>>> getOrdersByRestaurant(
			@PathVariable Integer restaurantId) {

		return orderService.fetchOrdersByRestaurant(restaurantId);
	}

	@PutMapping("/{orderId}/status")
	public ResponseEntity<ResponseStructure<OrderResponseDTO>> updateOrderStatus(@PathVariable Integer orderId,
			@RequestBody OrderStatusUpdateRequestDTO requestDTO) {

		return orderService.updateOrderStatus(orderId, requestDTO.getStatus());
	}

	@PutMapping("/{orderId}/cancel")
	public ResponseEntity<ResponseStructure<OrderResponseDTO>> cancelOrder(@PathVariable Integer orderId) {

		return orderService.cancelOrder(orderId);
	}

}
