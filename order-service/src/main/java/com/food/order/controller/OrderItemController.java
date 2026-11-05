package com.food.order.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.food.order.dto.AddItemRequest;
import com.food.order.dto.ResponseStructure;
import com.food.order.dto.UpdateQuantityRequest;
import com.food.order.entity.OrderItem;
import com.food.order.service.OrderItemService;

@RestController
@RequestMapping("/orderItem")
public class OrderItemController {

	@Autowired
	private OrderItemService orderItemService;

	@GetMapping("/{orderId}")
	public ResponseEntity<ResponseStructure<List<OrderItem>>> getItemsOfOrder(@PathVariable Integer orderId) {

		return orderItemService.getItemsOfOrder(orderId);
	}

	@GetMapping("/exists/menuItem/{menuItemId}")
	public ResponseEntity<ResponseStructure<Boolean>> existsByMenuItem(@PathVariable Integer menuItemId) {

		return orderItemService.existsByMenuItem(menuItemId);
	}

	@PostMapping("/{orderId}/items")
	public ResponseEntity<ResponseStructure<OrderItem>> addItemToOrder(@PathVariable Integer orderId,
			@RequestBody AddItemRequest request) {

		return orderItemService.addItemToExistingOrder(orderId, request.getMenuItemId(), request.getQuantity());
	}

	@PutMapping("/{orderItemId}")
	public ResponseEntity<ResponseStructure<OrderItem>> updateItemQuantity(@PathVariable Integer orderItemId,
			@RequestBody UpdateQuantityRequest request) {

		return orderItemService.updateItemQuantity(orderItemId, request);
	}

	@DeleteMapping("/{orderItemId}")
	public ResponseEntity<ResponseStructure<String>> removeItem(@PathVariable Integer orderItemId) {

		return orderItemService.removeItemFromOrder(orderItemId);
	}

}
