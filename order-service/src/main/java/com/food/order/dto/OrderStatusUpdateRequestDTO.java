package com.food.order.dto;

import com.food.order.enums.Status;

public class OrderStatusUpdateRequestDTO {

	private Status status;

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

}
