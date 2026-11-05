package com.food.customer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.food.customer.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

	boolean existsByEmailId(String emailId);

	boolean existsByContactNumber(Long contactNumber);

	Optional<Customer> findByContactNumber(Long contactNumber);

}
