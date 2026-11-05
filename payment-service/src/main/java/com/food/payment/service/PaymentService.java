package com.food.payment.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.food.payment.dao.PaymentDao;
import com.food.payment.dto.PaymentRequest;
import com.food.payment.dto.PaymentResponseDTO;
import com.food.payment.dto.ResponseStructure;
import com.food.payment.entity.Payment;
import com.food.payment.enums.PaymentMethod;
import com.food.payment.enums.PaymentStatus;
import com.food.payment.error.IdNotFoundException;
import com.food.payment.error.NoRecordFoundException;

@Service
public class PaymentService {

	@Autowired
	private PaymentDao paymentDao;

	private PaymentResponseDTO toDTO(Payment payment) {
		PaymentResponseDTO dto = new PaymentResponseDTO();
		dto.setPaymentId(payment.getPaymentId());
		dto.setPaymentMethod(payment.getPaymentMethod());
		dto.setPaymentStatus(payment.getPaymentStatus());
		dto.setAmount(payment.getAmount());
		dto.setOrderId(payment.getOrderId());
		return dto;
	}

	public ResponseEntity<ResponseStructure<PaymentResponseDTO>> createPayment(PaymentRequest request) {

		Payment payment = new Payment();
		payment.setOrderId(request.getOrderId());
		payment.setPaymentMethod(request.getPaymentMethod());
		payment.setPaymentStatus(
				request.getPaymentStatus() != null ? request.getPaymentStatus() : PaymentStatus.PENDING);
		payment.setAmount(request.getAmount());

		Payment saved = paymentDao.save(payment);

		ResponseStructure<PaymentResponseDTO> structure = new ResponseStructure<>();
		structure.setStatusCode(HttpStatus.CREATED.value());
		structure.setMessage("Payment created successfully");
		structure.setData(toDTO(saved));

		return new ResponseEntity<>(structure, HttpStatus.CREATED);
	}

	public ResponseEntity<ResponseStructure<PaymentResponseDTO>> getPaymentById(Integer paymentId) {

		Optional<Payment> optional = paymentDao.getPaymentById(paymentId);

		ResponseStructure<PaymentResponseDTO> structure = new ResponseStructure<>();

		if (optional.isPresent()) {

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage("Payment fetched successfully");
			structure.setData(toDTO(optional.get()));

			return new ResponseEntity<ResponseStructure<PaymentResponseDTO>>(structure, HttpStatus.OK);

		} else {
			throw new IdNotFoundException("Payment not found with given ID");
		}

	}

	public ResponseEntity<ResponseStructure<PaymentResponseDTO>> getPaymentByOrderId(Integer orderId) {

		Optional<Payment> optional = paymentDao.getPaymentByOrderId(orderId);

		ResponseStructure<PaymentResponseDTO> structure = new ResponseStructure<>();

		if (optional.isPresent()) {

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage("Payment fetched successfully");
			structure.setData(toDTO(optional.get()));

			return new ResponseEntity<ResponseStructure<PaymentResponseDTO>>(structure, HttpStatus.OK);

		} else {
			throw new NoRecordFoundException("No payment found for order id " + orderId);
		}

	}

	public ResponseEntity<ResponseStructure<List<PaymentResponseDTO>>> getPaymentByStatus(PaymentStatus status) {

		List<Payment> payments = paymentDao.getPaymentByStatus(status);

		ResponseStructure<List<PaymentResponseDTO>> structure = new ResponseStructure<>();

		if (payments != null && !payments.isEmpty()) {

			List<PaymentResponseDTO> dtoList = new ArrayList<>();

			for (Payment payment : payments) {
				dtoList.add(toDTO(payment));
			}

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage("Payments fetched successfully");
			structure.setData(dtoList);

			return new ResponseEntity<ResponseStructure<List<PaymentResponseDTO>>>(structure, HttpStatus.OK);

		} else {
			throw new NoRecordFoundException("No payments found with given status");
		}

	}

	public ResponseEntity<ResponseStructure<List<PaymentResponseDTO>>> getPaymentByMethod(PaymentMethod method) {

		List<Payment> payments = paymentDao.getPaymentByMethod(method);

		ResponseStructure<List<PaymentResponseDTO>> structure = new ResponseStructure<>();

		if (payments != null && !payments.isEmpty()) {

			List<PaymentResponseDTO> dtoList = new ArrayList<>();

			for (Payment payment : payments) {
				dtoList.add(toDTO(payment));
			}

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage("Payments fetched by method successfully");
			structure.setData(dtoList);

			return new ResponseEntity<ResponseStructure<List<PaymentResponseDTO>>>(structure, HttpStatus.OK);

		} else {
			throw new NoRecordFoundException("No payments found with given method");
		}

	}

	public ResponseEntity<ResponseStructure<PaymentResponseDTO>> updatePaymentStatus(Integer paymentId,
			PaymentStatus newStatus) {

		Optional<Payment> optional = paymentDao.getPaymentById(paymentId);

		ResponseStructure<PaymentResponseDTO> structure = new ResponseStructure<>();

		if (optional.isPresent()) {

			Payment payment = optional.get();
			PaymentStatus currentStatus = payment.getPaymentStatus();

			if (currentStatus == PaymentStatus.SUCCESS) {
				throw new IllegalStateException("Payment already completed. Status cannot be changed.");
			}

			if (currentStatus == PaymentStatus.PENDING) {
				if (!(newStatus == PaymentStatus.SUCCESS || newStatus == PaymentStatus.FAILED)) {
					throw new IllegalStateException("Invalid status transition from PENDING");
				}
			}

			if (currentStatus == PaymentStatus.FAILED) {
				if (!(newStatus == PaymentStatus.PENDING)) {
					throw new IllegalStateException("FAILED payment can only be retried to PENDING");
				}
			}

			payment.setPaymentStatus(newStatus);
			Payment updatedPayment = paymentDao.save(payment);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage("Payment status updated successfully");
			structure.setData(toDTO(updatedPayment));

			return new ResponseEntity<ResponseStructure<PaymentResponseDTO>>(structure, HttpStatus.OK);

		} else {
			throw new IdNotFoundException("Payment not found with given ID");
		}

	}

}
