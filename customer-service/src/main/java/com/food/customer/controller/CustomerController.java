package com.food.customer.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.food.customer.dto.ResponseStructure;
import com.food.customer.entity.Customer;
import com.food.customer.service.CustomerService;

@RestController
@RequestMapping("/customer")
public class CustomerController {

	@Autowired
	private CustomerService customerService;

	@PostMapping
	public ResponseEntity<ResponseStructure<Customer>> saveCustomer(@RequestBody Customer customer) {
		return customerService.saveCustomer(customer);
	}

	@GetMapping
	public ResponseEntity<ResponseStructure<List<Customer>>> fetchAllCustomers() {
		return customerService.getAllCustomers();
	}

	@GetMapping("/{id}")
	public ResponseEntity<ResponseStructure<Customer>> fetchCustomerById(@PathVariable Integer id) {
		return customerService.fetchCustomerById(id);
	}

	@GetMapping("/contact/{contact}")
	public ResponseEntity<ResponseStructure<Customer>> fetchCustomerByContact(@PathVariable Long contact) {
		return customerService.fetchCustomerByContact(contact);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ResponseStructure<Customer>> updateCustomer(@PathVariable Integer id,
			@RequestBody Customer customer) {
		return customerService.updateCustomer(id, customer);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ResponseStructure<String>> deleteCustomer(@PathVariable Integer id) {
		return customerService.deleteCustomer(id);
	}

}
