package com.ssdev.rsfinanceandinvestiments.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ssdev.rsfinanceandinvestiments.dto.CustomerRequest;
import com.ssdev.rsfinanceandinvestiments.dto.CustomerResponse;
import com.ssdev.rsfinanceandinvestiments.entity.Customer;
import com.ssdev.rsfinanceandinvestiments.repository.CustomerRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CustomerService {
	
	@Autowired
	private  CustomerRepository customerRepository;
	
	
	public Customer createCustomer(Customer request) {
       
		return customerRepository.save(request);
    }
	
	
	public List<Customer> findAll()
	{
		return customerRepository.findAll();
	}

}
