package com.food.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.food.payment.entity.Payment;
import com.food.payment.enums.PaymentMethod;
import com.food.payment.enums.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

	List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);

	List<Payment> findByPaymentMethod(PaymentMethod paymentMethod);

	Optional<Payment> findFirstByOrderId(Integer orderId);

}
