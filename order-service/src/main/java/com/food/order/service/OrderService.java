package com.food.order.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import feign.FeignException;

import com.food.order.client.CustomerClient;
import com.food.order.client.MenuItemClient;
import com.food.order.client.PaymentClient;
import com.food.order.dao.OrderDao;
import com.food.order.dto.CustomerResponseDTO;
import com.food.order.dto.MenuItemDTO;
import com.food.order.dto.OrderItemResponseDTO;
import com.food.order.dto.OrderResponseDTO;
import com.food.order.dto.PaymentRequest;
import com.food.order.dto.PaymentResponseDTO;
import com.food.order.dto.PlaceOrderRequest;
import com.food.order.dto.ResponseStructure;
import com.food.order.entity.Order;
import com.food.order.entity.OrderItem;
import com.food.order.enums.PaymentStatus;
import com.food.order.enums.Status;
import com.food.order.error.NoRecordFoundException;

@Service
public class OrderService {

	@Autowired
	private OrderDao orderDao;

	@Autowired
	private CustomerClient customerClient;

	@Autowired
	private MenuItemClient menuItemClient;

	@Autowired
	private PaymentClient paymentClient;

	/* ---------- Feign helpers ---------- */

	private CustomerResponseDTO fetchCustomer(Integer customerId) {
		try {
			ResponseStructure<CustomerResponseDTO> res = customerClient.getCustomer(customerId);
			if (res == null || res.getData() == null) {
				throw new NoRecordFoundException("Customer not found");
			}
			return res.getData();
		} catch (FeignException e) {
			throw new NoRecordFoundException("Customer not found");
		}
	}

	private MenuItemDTO fetchMenuItem(Integer menuItemId) {
		try {
			ResponseStructure<MenuItemDTO> res = menuItemClient.getMenuItem(menuItemId);
			if (res == null || res.getData() == null) {
				throw new NoRecordFoundException("Menu item not found");
			}
			return res.getData();
		} catch (FeignException e) {
			throw new NoRecordFoundException("Menu item not found");
		}
	}

	// For read paths: degrade gracefully if customer/payment services are unreachable
	private CustomerResponseDTO fetchCustomerSummary(Integer customerId) {
		try {
			ResponseStructure<CustomerResponseDTO> res = customerClient.getCustomer(customerId);
			if (res != null && res.getData() != null) {
				return res.getData();
			}
		} catch (FeignException e) {
			// fall through
		}
		CustomerResponseDTO dto = new CustomerResponseDTO();
		dto.setCustomerId(customerId);
		return dto;
	}

	private PaymentResponseDTO fetchPaymentSummary(Integer orderId) {
		try {
			ResponseStructure<PaymentResponseDTO> res = paymentClient.getPaymentByOrder(orderId);
			if (res != null) {
				return res.getData();
			}
		} catch (FeignException e) {
			// fall through
		}
		return null;
	}

	/* ---------- DTO mapping ---------- */

	private OrderItemResponseDTO toItemDTO(OrderItem item) {
		OrderItemResponseDTO dto = new OrderItemResponseDTO();
		dto.setOrderItemId(item.getOrderItemId());
		dto.setItemName(item.getItemName());
		dto.setPrice(item.getPrice() != null ? item.getPrice() : 0);
		dto.setQuantity(item.getQuantity());
		dto.setSubTotal(item.getSubTotal() != null ? item.getSubTotal() : 0);
		return dto;
	}

	private OrderResponseDTO toOrderDTO(Order order) {
		OrderResponseDTO orderDTO = new OrderResponseDTO();
		orderDTO.setOrderId(order.getOrderId());
		orderDTO.setOrderDateTime(order.getOrderDateTime());
		orderDTO.setStatus(order.getStatus());
		orderDTO.setTotalAmount(order.getTotalAmount());
		orderDTO.setCustomer(fetchCustomerSummary(order.getCustomerId()));
		orderDTO.setPayment(fetchPaymentSummary(order.getOrderId()));

		List<OrderItemResponseDTO> itemDTOList = new ArrayList<>();
		if (order.getOrderItems() != null) {
			for (OrderItem item : order.getOrderItems()) {
				itemDTOList.add(toItemDTO(item));
			}
		}
		orderDTO.setOrderItems(itemDTOList);
		return orderDTO;
	}

	private ResponseEntity<ResponseStructure<List<OrderResponseDTO>>> toOrderListResponse(
			List<Order> orders, String message) {

		if (orders.isEmpty()) {
			throw new NoRecordFoundException(message);
		}

		List<OrderResponseDTO> orderDTOList = new ArrayList<>();
		for (Order order : orders) {
			orderDTOList.add(toOrderDTO(order));
		}

		ResponseStructure<List<OrderResponseDTO>> res = new ResponseStructure<>();
		res.setStatusCode(HttpStatus.OK.value());
		res.setMessage(message);
		res.setData(orderDTOList);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	/* ---------- Business methods ---------- */

	public ResponseEntity<ResponseStructure<OrderResponseDTO>> placeOrder(PlaceOrderRequest request) {

		if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
			throw new IllegalStateException("Order must contain at least one item");
		}
		if (request.getCustomer() == null || request.getCustomer().getCustomerId() == null) {
			throw new IllegalStateException("Customer must be provided");
		}
		Integer customerId = request.getCustomer().getCustomerId();
		CustomerResponseDTO customerDTO = fetchCustomer(customerId);

		if (request.getPayment() == null) {
			throw new IllegalStateException("Payment must be provided before placing order");
		}

		Order order = new Order();
		order.setCustomerId(customerId);

		double totalAmount = 0;
		List<OrderItem> orderItems = new ArrayList<>();

		for (PlaceOrderRequest.OrderItemRequest itemReq : request.getOrderItems()) {
			if (itemReq.getQuantity() == null || itemReq.getQuantity() < 1) {
				throw new IllegalStateException("Order quantity must be minimum 1");
			}
			Integer menuItemId = itemReq.getMenuItem() != null ? itemReq.getMenuItem().getItemId() : null;
			if (menuItemId == null) {
				throw new IllegalStateException("MenuItem must be provided for each order item");
			}

			MenuItemDTO menuItem = fetchMenuItem(menuItemId);
			if (!Boolean.TRUE.equals(menuItem.getAvailability())) {
				throw new IllegalStateException(menuItem.getItemName() + " is currently unavailable");
			}

			OrderItem item = new OrderItem();
			item.setMenuItemId(menuItemId);
			item.setItemName(menuItem.getItemName());
			item.setPrice(menuItem.getPrice());
			item.setRestaurantId(menuItem.getRestaurantId());
			item.setQuantity(itemReq.getQuantity());

			double subTotal = menuItem.getPrice() * itemReq.getQuantity();
			item.setSubTotal(subTotal);
			totalAmount += subTotal;

			item.setOrder(order);
			orderItems.add(item);
		}

		order.setOrderItems(orderItems);
		order.setTotalAmount(totalAmount);

		PlaceOrderRequest.PaymentInfo paymentInfo = request.getPayment();
		if (paymentInfo.getPaymentStatus() == PaymentStatus.FAILED) {
			order.setStatus(Status.PENDING_PAYMENT);
		} else {
			order.setStatus(Status.PLACED);
		}

		Order savedOrder = orderDao.saveOrder(order);

		// Create payment in payment-service
		PaymentRequest paymentRequest = new PaymentRequest();
		paymentRequest.setOrderId(savedOrder.getOrderId());
		paymentRequest.setPaymentMethod(paymentInfo.getPaymentMethod());
		paymentRequest.setPaymentStatus(paymentInfo.getPaymentStatus());
		paymentRequest.setAmount(totalAmount);

		PaymentResponseDTO paymentDTO;
		try {
			ResponseStructure<PaymentResponseDTO> payRes = paymentClient.createPayment(paymentRequest);
			if (payRes == null || payRes.getData() == null) {
				throw new IllegalStateException("Payment creation failed");
			}
			paymentDTO = payRes.getData();
		} catch (FeignException e) {
			throw new IllegalStateException("Payment service is currently unavailable");
		}

		savedOrder.setPaymentId(paymentDTO.getPaymentId());
		savedOrder = orderDao.saveOrder(savedOrder);

		OrderResponseDTO orderDTO = toOrderDTO(savedOrder);
		orderDTO.setCustomer(customerDTO);
		orderDTO.setPayment(paymentDTO);

		ResponseStructure<OrderResponseDTO> res = new ResponseStructure<>();
		res.setStatusCode(HttpStatus.CREATED.value());
		res.setMessage("Order placed successfully");
		res.setData(orderDTO);
		return new ResponseEntity<ResponseStructure<OrderResponseDTO>>(res, HttpStatus.CREATED);
	}

	public ResponseEntity<ResponseStructure<List<OrderResponseDTO>>> fetchAllOrders() {
		List<Order> orders = orderDao.fetchAllOrders();
		return toOrderListResponse(orders, "All orders fetched successfully");
	}

	public ResponseEntity<ResponseStructure<OrderResponseDTO>> fetchOrderById(Integer orderId) {

		Optional<Order> optionalOrder = orderDao.fetchOrderById(orderId);

		if (optionalOrder.isEmpty()) {
			throw new NoRecordFoundException("Order not found");
		}

		ResponseStructure<OrderResponseDTO> res = new ResponseStructure<>();
		res.setStatusCode(HttpStatus.OK.value());
		res.setMessage("Order fetched successfully");
		res.setData(toOrderDTO(optionalOrder.get()));

		return new ResponseEntity<ResponseStructure<OrderResponseDTO>>(res, HttpStatus.OK);
	}

	public ResponseEntity<ResponseStructure<List<OrderResponseDTO>>> fetchOrdersOfCustomer(Integer customerId) {
		List<Order> orders = orderDao.fetchOrdersOfCustomer(customerId);
		return toOrderListResponse(orders, "Customer orders fetched successfully");
	}

	public ResponseEntity<ResponseStructure<List<OrderResponseDTO>>> fetchOrdersByStatus(Status status) {
		List<Order> orders = orderDao.fetchOrdersByStatus(status);
		return toOrderListResponse(orders, "Orders fetched successfully by status");
	}

	public ResponseEntity<ResponseStructure<List<OrderResponseDTO>>> fetchOrdersByDate(LocalDate date) {

		LocalDateTime start = date.atStartOfDay();
		LocalDateTime end = date.atTime(23, 59, 59);

		List<Order> orders = orderDao.fetchOrdersByDate(start, end);
		return toOrderListResponse(orders, "Orders fetched successfully by date");
	}

	public ResponseEntity<ResponseStructure<List<OrderResponseDTO>>> fetchOrdersBetweenTotalAmountRange(
			Double minAmount, Double maxAmount) {

		List<Order> orders = orderDao.fetchOrdersBetweenTotalAmountRange(minAmount, maxAmount);
		return toOrderListResponse(orders, "Orders fetched successfully between total amount range");
	}

	public ResponseEntity<ResponseStructure<List<OrderResponseDTO>>> fetchOrdersByRestaurant(Integer restaurantId) {

		List<Order> orders = orderDao.fetchOrdersByRestaurant(restaurantId);
		return toOrderListResponse(orders, "Orders fetched successfully for restaurant");
	}

	public ResponseEntity<ResponseStructure<OrderResponseDTO>> updateOrderStatus(Integer orderId, Status newStatus) {

		Optional<Order> optional = orderDao.fetchOrderById(orderId);

		if (optional.isEmpty()) {
			throw new NoRecordFoundException("Order not found with id " + orderId);
		}

		Order order = optional.get();
		Status currentStatus = order.getStatus();

		// Handle cancellation separately
		if (newStatus == Status.CANCELLED) {

			if (currentStatus == Status.PREPARING || currentStatus == Status.DELIVERED) {
				throw new IllegalStateException("Order cannot be cancelled after preparation started");
			}

		} else {

			// prevent backward status change
			if (newStatus.ordinal() < currentStatus.ordinal()) {
				throw new IllegalStateException("Invalid status transition from " + currentStatus + " to " + newStatus);
			}

			// prevent skipping status
			if (newStatus.ordinal() > currentStatus.ordinal() + 1) {
				throw new IllegalStateException("Order status must follow sequential flow");
			}
		}

		order.setStatus(newStatus);
		Order updatedOrder = orderDao.saveOrder(order);

		ResponseStructure<OrderResponseDTO> res = new ResponseStructure<>();
		res.setStatusCode(HttpStatus.OK.value());
		res.setMessage("Order status updated successfully");
		res.setData(toOrderDTO(updatedOrder));

		return new ResponseEntity<ResponseStructure<OrderResponseDTO>>(res, HttpStatus.OK);
	}

	public ResponseEntity<ResponseStructure<Boolean>> existsByCustomer(Integer customerId) {

		ResponseStructure<Boolean> res = new ResponseStructure<>();
		res.setStatusCode(HttpStatus.OK.value());
		res.setMessage("Order existence check completed");
		res.setData(orderDao.existsByCustomer(customerId));

		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	public ResponseEntity<ResponseStructure<OrderResponseDTO>> cancelOrder(Integer orderId) {

		Optional<Order> optional = orderDao.fetchOrderById(orderId);

		if (optional.isEmpty()) {
			throw new NoRecordFoundException("Order not found with id " + orderId);
		}

		Order order = optional.get();

		Status currentStatus = order.getStatus();

		if (currentStatus == Status.PREPARING || currentStatus == Status.DELIVERED) {
			throw new IllegalStateException("Order cannot be cancelled after preparation started");
		}

		order.setStatus(Status.CANCELLED);

		Order updatedOrder = orderDao.saveOrder(order);

		ResponseStructure<OrderResponseDTO> res = new ResponseStructure<>();
		res.setStatusCode(HttpStatus.OK.value());
		res.setMessage("Order cancelled successfully");
		res.setData(toOrderDTO(updatedOrder));

		return new ResponseEntity<ResponseStructure<OrderResponseDTO>>(res, HttpStatus.OK);
	}

}
