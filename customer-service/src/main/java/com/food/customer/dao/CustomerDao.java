package com.food.customer.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.food.customer.entity.Customer;
import com.food.customer.repository.CustomerRepository;

@Repository
public class CustomerDao {

	@Autowired
	private CustomerRepository customerRepository;

	public Customer saveCustomer(Customer customer) {
		return customerRepository.save(customer);
	}

	public List<Customer> getAllCustomers() {
		return customerRepository.findAll();
	}

	public Optional<Customer> fetchCustomerById(Integer id) {
		return customerRepository.findById(id);
	}

	public Optional<Customer> fetchCustomerByContact(Long contact) {
		return customerRepository.findByContactNumber(contact);
	}

	public void deleteCustomer(Integer id) {
		customerRepository.deleteById(id);
	}

	public boolean emailExists(String email) {
		return customerRepository.existsByEmailId(email);
	}

	public boolean contactExists(Long contact) {
		return customerRepository.existsByContactNumber(contact);
	}

}
