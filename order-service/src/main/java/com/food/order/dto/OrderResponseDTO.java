package com.food.order.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.food.order.enums.Status;

public class OrderResponseDTO {

	private Integer orderId;
	private LocalDateTime orderDateTime;
	private Status status;
	private double totalAmount;
	private CustomerResponseDTO customer;
	private PaymentResponseDTO payment;
	private List<OrderItemResponseDTO> orderItems;

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	public LocalDateTime getOrderDateTime() {
		return orderDateTime;
	}

	public void setOrderDateTime(LocalDateTime orderDateTime) {
		this.orderDateTime = orderDateTime;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public CustomerResponseDTO getCustomer() {
		return customer;
	}

	public void setCustomer(CustomerResponseDTO customer) {
		this.customer = customer;
	}

	public PaymentResponseDTO getPayment() {
		return payment;
	}

	public void setPayment(PaymentResponseDTO payment) {
		this.payment = payment;
	}

	public List<OrderItemResponseDTO> getOrderItems() {
		return orderItems;
	}

	public void setOrderItems(List<OrderItemResponseDTO> orderItems) {
		this.orderItems = orderItems;
	}

}
