package com.food.customer.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import feign.FeignException;

import com.food.customer.client.OrderClient;
import com.food.customer.dao.CustomerDao;
import com.food.customer.dto.ResponseStructure;
import com.food.customer.entity.Customer;
import com.food.customer.error.NoRecordFoundException;

@Service
public class CustomerService {

	@Autowired
	private CustomerDao customerDao;

	@Autowired
	private OrderClient orderClient;

	public ResponseEntity<ResponseStructure<Customer>> saveCustomer(Customer customer) {

		String email = customer.getEmailId();
		Long contact = customer.getContactNumber();

		if (customerDao.emailExists(email)) {
			throw new DataIntegrityViolationException("Email already exists");
		}

		if (customerDao.contactExists(contact)) {
			throw new DataIntegrityViolationException("Contact number already exists");
		}

		if (String.valueOf(contact).length() != 10) {
			throw new DataIntegrityViolationException("Contact number must be exactly 10 digits");
		}

		Customer c = customerDao.saveCustomer(customer);
		ResponseStructure<Customer> res = new ResponseStructure<>();

		res.setStatusCode(HttpStatus.CREATED.value());
		res.setMessage("Saved");
		res.setData(c);
		return new ResponseEntity<ResponseStructure<Customer>>(res, HttpStatus.CREATED);

	}

	public ResponseEntity<ResponseStructure<List<Customer>>> getAllCustomers() {

		List<Customer> customers = customerDao.getAllCustomers();

		ResponseStructure<List<Customer>> res = new ResponseStructure<>();

		if (!customers.isEmpty()) {

			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Customers fetched successfully");
			res.setData(customers);

			return new ResponseEntity<ResponseStructure<List<Customer>>>(res, HttpStatus.OK);

		} else {

			throw new NoRecordFoundException("Customer table is empty");

		}
	}

	public ResponseEntity<ResponseStructure<Customer>> fetchCustomerById(Integer id) {

		Optional<Customer> optional = customerDao.fetchCustomerById(id);

		ResponseStructure<Customer> res = new ResponseStructure<>();

		if (optional.isPresent()) {

			Customer customer = optional.get();

			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Customer fetched successfully");
			res.setData(customer);

			return new ResponseEntity<ResponseStructure<Customer>>(res, HttpStatus.OK);

		} else {

			throw new NoRecordFoundException("Customer ID not found");

		}

	}

	public ResponseEntity<ResponseStructure<Customer>> fetchCustomerByContact(Long contact) {

		Optional<Customer> optional = customerDao.fetchCustomerByContact(contact);

		ResponseStructure<Customer> res = new ResponseStructure<>();

		if (optional.isPresent()) {

			Customer customer = optional.get();

			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Customer fetched successfully");
			res.setData(customer);

			return new ResponseEntity<ResponseStructure<Customer>>(res, HttpStatus.OK);

		} else {

			throw new NoRecordFoundException("Customer with this contact number not found");

		}
	}

	public ResponseEntity<ResponseStructure<Customer>> updateCustomer(Integer id, Customer customer) {

		Optional<Customer> optional = customerDao.fetchCustomerById(id);

		if (optional.isPresent()) {

			Customer existing = optional.get();

			String email = customer.getEmailId();
			Long contact = customer.getContactNumber();

			if (customerDao.emailExists(email) && !existing.getEmailId().equals(email)) {
				throw new DataIntegrityViolationException("Email already exists");
			}

			if (customerDao.contactExists(contact) && !existing.getContactNumber().equals(contact)) {
				throw new DataIntegrityViolationException("Contact number already exists");
			}

			if (String.valueOf(contact).length() != 10) {
				throw new DataIntegrityViolationException("Contact number must be exactly 10 digits");
			}

			existing.setCustomerName(customer.getCustomerName());
			existing.setEmailId(email);
			existing.setContactNumber(contact);
			existing.setAddress(customer.getAddress());

			Customer updated = customerDao.saveCustomer(existing);

			ResponseStructure<Customer> res = new ResponseStructure<>();

			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Customer updated successfully");
			res.setData(updated);

			return new ResponseEntity<ResponseStructure<Customer>>(res, HttpStatus.OK);

		} else {
			throw new NoRecordFoundException("Customer ID not found");
		}
	}

	public ResponseEntity<ResponseStructure<String>> deleteCustomer(Integer id) {

		Optional<Customer> optional = customerDao.fetchCustomerById(id);

		if (optional.isPresent()) {

			// Check via order-service whether this customer has any orders
			try {
				ResponseStructure<Boolean> exists = orderClient.existsByCustomer(id);
				if (exists != null && Boolean.TRUE.equals(exists.getData())) {
					throw new IllegalStateException("Customer has orders, deletion not allowed");
				}
			} catch (FeignException e) {
				throw new IllegalStateException("Cannot verify customer orders right now");
			}

			customerDao.deleteCustomer(id);

			ResponseStructure<String> res = new ResponseStructure<>();

			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Customer deleted successfully");
			res.setData("Deleted!");

			return new ResponseEntity<ResponseStructure<String>>(res, HttpStatus.OK);

		} else {

			throw new NoRecordFoundException("Customer ID not found");

		}
	}
}
