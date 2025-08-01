package com.ssdev.rsfinanceandinvestiments.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ssdev.rsfinanceandinvestiments.entity.Customer;


@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long> {
	
	Optional<Customer> findByPhoneNumber(String phoneNumber);


}
