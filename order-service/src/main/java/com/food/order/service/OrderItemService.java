package com.food.order.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import feign.FeignException;

import com.food.order.client.MenuItemClient;
import com.food.order.dao.OrderDao;
import com.food.order.dao.OrderItemDao;
import com.food.order.dto.MenuItemDTO;
import com.food.order.dto.ResponseStructure;
import com.food.order.dto.UpdateQuantityRequest;
import com.food.order.entity.Order;
import com.food.order.entity.OrderItem;
import com.food.order.enums.Status;
import com.food.order.repository.OrderItemRepository;
import com.food.order.repository.OrderRepository;

@Service
public class OrderItemService {

	@Autowired
	private OrderItemDao orderItemDao;

	@Autowired
	private OrderDao orderDao;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private OrderItemRepository orderItemRepository;

	@Autowired
	private MenuItemClient menuItemClient;

	private MenuItemDTO fetchMenuItemOrNull(Integer menuItemId) {
		try {
			var res = menuItemClient.getMenuItem(menuItemId);
			return res != null ? res.getData() : null;
		} catch (FeignException e) {
			return null;
		}
	}

	public ResponseEntity<ResponseStructure<OrderItem>> addItemToExistingOrder(Integer orderId, Integer menuItemId,
			Integer quantity) {

		ResponseStructure<OrderItem> structure = new ResponseStructure<>();

		Optional<Order> optionalOrder = orderRepository.findById(orderId);
		MenuItemDTO menuItem = fetchMenuItemOrNull(menuItemId);

		if (optionalOrder.isEmpty() || menuItem == null) {

			structure.setStatusCode(HttpStatus.NOT_FOUND.value());
			structure.setMessage("Order or MenuItem not found");
			structure.setData(null);

			return new ResponseEntity<>(structure, HttpStatus.NOT_FOUND);
		}

		Order order = optionalOrder.get();

		List<OrderItem> existingItems = orderItemRepository.findByOrderOrderIdAndMenuItemId(orderId, menuItemId);

		OrderItem orderItem;

		if (!existingItems.isEmpty()) {

			orderItem = existingItems.get(0);

			int newQuantity = orderItem.getQuantity() + quantity;
			orderItem.setQuantity(newQuantity);

			// refresh snapshot from menu-service
			orderItem.setItemName(menuItem.getItemName());
			orderItem.setPrice(menuItem.getPrice());
			orderItem.setRestaurantId(menuItem.getRestaurantId());

			double subtotal = newQuantity * menuItem.getPrice();
			orderItem.setSubTotal(subtotal);

		} else {

			orderItem = new OrderItem();
			orderItem.setOrder(order);
			orderItem.setMenuItemId(menuItemId);
			orderItem.setItemName(menuItem.getItemName());
			orderItem.setPrice(menuItem.getPrice());
			orderItem.setRestaurantId(menuItem.getRestaurantId());
			orderItem.setQuantity(quantity);

			double subtotal = quantity * menuItem.getPrice();
			orderItem.setSubTotal(subtotal);
		}

		OrderItem savedItem = orderItemRepository.save(orderItem);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setMessage("Item added/updated successfully");
		structure.setData(savedItem);

		return new ResponseEntity<ResponseStructure<OrderItem>>(structure, HttpStatus.OK);
	}

	public ResponseEntity<ResponseStructure<OrderItem>> updateItemQuantity(Integer orderItemId,
			UpdateQuantityRequest request) {

		ResponseStructure<OrderItem> structure = new ResponseStructure<>();

		Optional<OrderItem> optional = orderItemDao.findById(orderItemId);

		if (optional.isPresent()) {

			OrderItem item = optional.get();

			Integer quantity = request.getQuantity();
			item.setQuantity(quantity);

			// use live price from menu-service, fall back to stored snapshot
			MenuItemDTO menuItem = fetchMenuItemOrNull(item.getMenuItemId());
			double price = menuItem != null && menuItem.getPrice() != null ? menuItem.getPrice()
					: (item.getPrice() != null ? item.getPrice() : 0);
			item.setSubTotal(price * quantity);

			OrderItem updatedItem = orderItemDao.saveOrderItem(item);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage("Order item quantity updated successfully");
			structure.setData(updatedItem);

			return new ResponseEntity<>(structure, HttpStatus.OK);
		}

		structure.setStatusCode(HttpStatus.NOT_FOUND.value());
		structure.setMessage("Order item not found");
		structure.setData(null);

		return new ResponseEntity<>(structure, HttpStatus.NOT_FOUND);
	}

	public ResponseEntity<ResponseStructure<String>> removeItemFromOrder(Integer orderItemId) {

		ResponseStructure<String> structure = new ResponseStructure<>();

		Optional<OrderItem> optional = orderItemDao.findById(orderItemId);

		if (optional.isPresent()) {

			OrderItem item = optional.get();
			Order order = item.getOrder();

			if (order.getStatus() == Status.PREPARING || order.getStatus() == Status.DELIVERED) {

				structure.setStatusCode(HttpStatus.BAD_REQUEST.value());
				structure.setMessage("Cannot modify order after preparation started");
				structure.setData(null);

				return new ResponseEntity<>(structure, HttpStatus.BAD_REQUEST);
			}

			// subtract item subtotal from order total
			double updatedTotal = Math.max(0, order.getTotalAmount() - item.getSubTotal());
			order.setTotalAmount(updatedTotal);

			// remove item from order list
			order.getOrderItems().remove(item);

			orderDao.saveOrder(order);
			orderItemDao.deleteOrderItem(item);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage("Item removed from order successfully");
			structure.setData("Deleted");

			return new ResponseEntity<ResponseStructure<String>>(structure, HttpStatus.OK);
		}

		structure.setStatusCode(HttpStatus.NOT_FOUND.value());
		structure.setMessage("Order item not found");
		structure.setData(null);

		return new ResponseEntity<ResponseStructure<String>>(structure, HttpStatus.NOT_FOUND);
	}

	public ResponseEntity<ResponseStructure<List<OrderItem>>> getItemsOfOrder(Integer orderId) {

		ResponseStructure<List<OrderItem>> structure = new ResponseStructure<>();

		Optional<Order> optional = orderRepository.findById(orderId);

		if (optional.isPresent()) {

			List<OrderItem> items = orderItemDao.findItemsByOrderId(orderId);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage("Order items fetched successfully");
			structure.setData(items);

			return new ResponseEntity<ResponseStructure<List<OrderItem>>>(structure, HttpStatus.OK);
		}

		structure.setStatusCode(HttpStatus.NOT_FOUND.value());
		structure.setMessage("Order not found");
		structure.setData(null);

		return new ResponseEntity<ResponseStructure<List<OrderItem>>>(structure, HttpStatus.NOT_FOUND);
	}

	public ResponseEntity<ResponseStructure<Boolean>> existsByMenuItem(Integer menuItemId) {

		ResponseStructure<Boolean> structure = new ResponseStructure<>();
		structure.setStatusCode(HttpStatus.OK.value());
		structure.setMessage("Order item existence check completed");
		structure.setData(orderItemDao.existsByMenuItem(menuItemId));

		return new ResponseEntity<>(structure, HttpStatus.OK);
	}

}
