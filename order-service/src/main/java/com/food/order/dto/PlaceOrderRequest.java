package com.food.order.dto;

import com.food.order.enums.PaymentMethod;
import com.food.order.enums.PaymentStatus;

public class PlaceOrderRequest {

	private CustomerRef customer;
	private java.util.List<OrderItemRequest> orderItems;
	private PaymentInfo payment;

	public CustomerRef getCustomer() {
		return customer;
	}

	public void setCustomer(CustomerRef customer) {
		this.customer = customer;
	}

	public java.util.List<OrderItemRequest> getOrderItems() {
		return orderItems;
	}

	public void setOrderItems(java.util.List<OrderItemRequest> orderItems) {
		this.orderItems = orderItems;
	}

	public PaymentInfo getPayment() {
		return payment;
	}

	public void setPayment(PaymentInfo payment) {
		this.payment = payment;
	}

	public static class CustomerRef {
		private Integer customerId;

		public Integer getCustomerId() {
			return customerId;
		}

		public void setCustomerId(Integer customerId) {
			this.customerId = customerId;
		}
	}

	public static class OrderItemRequest {
		private MenuItemRef menuItem;
		private Integer quantity;

		public MenuItemRef getMenuItem() {
			return menuItem;
		}

		public void setMenuItem(MenuItemRef menuItem) {
			this.menuItem = menuItem;
		}

		public Integer getQuantity() {
			return quantity;
		}

		public void setQuantity(Integer quantity) {
			this.quantity = quantity;
		}
	}

	public static class MenuItemRef {
		private Integer itemId;

		public Integer getItemId() {
			return itemId;
		}

		public void setItemId(Integer itemId) {
			this.itemId = itemId;
		}
	}

	public static class PaymentInfo {
		private PaymentMethod paymentMethod;
		private PaymentStatus paymentStatus;

		public PaymentMethod getPaymentMethod() {
			return paymentMethod;
		}

		public void setPaymentMethod(PaymentMethod paymentMethod) {
			this.paymentMethod = paymentMethod;
		}

		public PaymentStatus getPaymentStatus() {
			return paymentStatus;
		}

		public void setPaymentStatus(PaymentStatus paymentStatus) {
			this.paymentStatus = paymentStatus;
		}
	}

}
