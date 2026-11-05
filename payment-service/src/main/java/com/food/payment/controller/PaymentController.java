package com.food.payment.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.food.payment.dto.PaymentRequest;
import com.food.payment.dto.PaymentResponseDTO;
import com.food.payment.dto.ResponseStructure;
import com.food.payment.enums.PaymentMethod;
import com.food.payment.enums.PaymentStatus;
import com.food.payment.service.PaymentService;

@RestController
@RequestMapping("/payment")
public class PaymentController {

	@Autowired
	private PaymentService paymentService;

	@PostMapping
	public ResponseEntity<ResponseStructure<PaymentResponseDTO>> createPayment(@RequestBody PaymentRequest request) {

		return paymentService.createPayment(request);
	}

	@GetMapping("/{paymentId}")
	public ResponseEntity<ResponseStructure<PaymentResponseDTO>> getById(@PathVariable Integer paymentId) {

		return paymentService.getPaymentById(paymentId);
	}

	@GetMapping("/order/{orderId}")
	public ResponseEntity<ResponseStructure<PaymentResponseDTO>> getByOrderId(@PathVariable Integer orderId) {

		return paymentService.getPaymentByOrderId(orderId);
	}

	@GetMapping("/status/{status}")
	public ResponseEntity<ResponseStructure<List<PaymentResponseDTO>>> getByStatus(@PathVariable PaymentStatus status) {

		return paymentService.getPaymentByStatus(status);
	}

	@GetMapping("/method/{method}")
	public ResponseEntity<ResponseStructure<List<PaymentResponseDTO>>> getByMethod(@PathVariable PaymentMethod method) {

		return paymentService.getPaymentByMethod(method);
	}

	@PutMapping("/pId/{paymentId}/status/{status}")
	public ResponseEntity<ResponseStructure<PaymentResponseDTO>> updateStatus(@PathVariable Integer paymentId,
			@PathVariable PaymentStatus status) {

		return paymentService.updatePaymentStatus(paymentId, status);
	}

}
