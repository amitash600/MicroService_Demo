package com.food.payment.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.food.payment.entity.Payment;
import com.food.payment.enums.PaymentMethod;
import com.food.payment.enums.PaymentStatus;
import com.food.payment.repository.PaymentRepository;

@Repository
public class PaymentDao {

	@Autowired
	private PaymentRepository paymentRepository;

	public Optional<Payment> getPaymentById(Integer paymentId) {
		return paymentRepository.findById(paymentId);
	}

	public Optional<Payment> getPaymentByOrderId(Integer orderId) {
		return paymentRepository.findFirstByOrderId(orderId);
	}

	public List<Payment> getPaymentByStatus(PaymentStatus status) {
		return paymentRepository.findByPaymentStatus(status);
	}

	public List<Payment> getPaymentByMethod(PaymentMethod method) {
		return paymentRepository.findByPaymentMethod(method);
	}

	public Payment save(Payment payment) {
		return paymentRepository.save(payment);
	}

}
