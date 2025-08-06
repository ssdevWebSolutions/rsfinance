package com.ssdev.rsfinanceandinvestiments.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ssdev.rsfinanceandinvestiments.dto.CustomerUpdateRequest;
import com.ssdev.rsfinanceandinvestiments.dto.EMIScheduleResponse;
import com.ssdev.rsfinanceandinvestiments.dto.RecentPayerResponse;
import com.ssdev.rsfinanceandinvestiments.dto.UpdateStatusRequest;
import com.ssdev.rsfinanceandinvestiments.entity.Customer;
import com.ssdev.rsfinanceandinvestiments.entity.EMISchedule;
import com.ssdev.rsfinanceandinvestiments.repository.CustomerRepository;
import com.ssdev.rsfinanceandinvestiments.repository.EMIScheduleRepository;
import com.ssdev.rsfinanceandinvestiments.service.CustomerService;
import com.ssdev.rsfinanceandinvestiments.service.EMIScheduleService;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CustomerController {
    
	@Autowired
    private  CustomerService customerService;
    
	@Autowired
    private EMIScheduleService emiScheduleService;
	
	
	@Autowired
	private EMIScheduleRepository emiScheduleRepository;
	
	@Autowired
	private CustomerRepository customerRepository;
	
	 private static final Logger log = LoggerFactory.getLogger(EMIScheduleService.class);

    
	 @PostMapping("/customers")
	 public ResponseEntity<String> createCustomer(@RequestBody Customer request) {
	     try {
	         log.info("🔥 Creating customer: {}", request.getName());

	         // 1. Save customer
	         Customer customer = customerService.createCustomer(request);

	         // 2. Trigger async EMI generation
	         emiScheduleService.generateEMIScheduleForCustomer(customer);

	         log.info("✅ Customer created successfully, EMI schedule generation started in background: {}", customer.getPhoneNumber());
	         
	         // ✅ Immediate response to client
	         return ResponseEntity.ok("inserted Successfully");

	     } catch (Exception e) {
	         log.error("❌ Error creating customer: {}", e.getMessage(), e);
	         return ResponseEntity.status(500).body("Error creating customer: " + e.getMessage());
	     }
	 }
	 
	// 🔥 NEW: UPDATE CUSTOMER
	    @PutMapping("/customers/{phoneNumber}")
	    public ResponseEntity<String> updateCustomer(
	            @PathVariable String phoneNumber, 
	            @RequestBody @Valid CustomerUpdateRequest request) {
	        try {
	            log.info("🔥 Updating customer with phone number: {}", phoneNumber);
	            
	            // Check if customer exists
	            Optional<Customer> existingCustomer = customerRepository.findByPhoneNumber(phoneNumber);
	            if (existingCustomer.isEmpty()) {
	                log.warn("❌ Customer not found with phone number: {}", phoneNumber);
	                return ResponseEntity.status(404).body("Customer not found");
	            }

	            // Update customer
	            Customer updatedCustomer = customerService.updateCustomer(phoneNumber, request);
	            
	            log.info("✅ Customer updated successfully: {}", updatedCustomer.getName());
	            return ResponseEntity.ok("Customer updated successfully");

	        } catch (Exception e) {
	            log.error("❌ Error updating customer: {}", e.getMessage(), e);
	            return ResponseEntity.status(500).body("Error updating customer: " + e.getMessage());
	        }
	    }

	    // 🔥 NEW: DELETE CUSTOMER
	    @DeleteMapping("/customers/{phoneNumber}")
	    public ResponseEntity<String> deleteCustomer(@PathVariable String phoneNumber) {
	        try {
	            log.info("🔥 Deleting customer with phone number: {}", phoneNumber);
	            
	            // Check if customer exists
	            Optional<Customer> existingCustomer = customerRepository.findByPhoneNumber(phoneNumber);
	            if (existingCustomer.isEmpty()) {
	                log.warn("❌ Customer not found with phone number: {}", phoneNumber);
	                return ResponseEntity.status(404).body("Customer not found");
	            }

	            // Delete customer (this will cascade delete EMI schedules if configured)
	            customerService.deleteCustomer(phoneNumber);
	            
	            log.info("✅ Customer deleted successfully: {}", phoneNumber);
	            return ResponseEntity.ok("Customer deleted successfully");

	        } catch (Exception e) {
	            log.error("❌ Error deleting customer: {}", e.getMessage(), e);
	            return ResponseEntity.status(500).body("Error deleting customer: " + e.getMessage());
	        }
	    }

    


    
    
    
    
    
    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> findAllCustomers() {
        try {
            List<Customer> customer = customerService.findAll();  
            return new ResponseEntity<>(customer, HttpStatus.CREATED);
        } catch (Exception ex) { 
            throw ex; // Let GlobalExceptionHandler handle it
        }
    }
    
    
   @GetMapping("/customers/recent-payers")
   public ResponseEntity<List<RecentPayerResponse>> getRecent() {
    Pageable top20 = PageRequest.of(0, 20);
    List<EMISchedule> recentPaidEMIs = emiScheduleRepository.findTop20PaidWithCustomer(top20);

    Set<String> phoneNumbers = recentPaidEMIs.stream()
        .map(EMISchedule::getCustomerPhone)
        .collect(Collectors.toCollection(LinkedHashSet::new)); // Keep order, avoid duplicates

    List<RecentPayerResponse> responseList = new ArrayList<>();

    for (String phone : phoneNumbers) {
        Optional<Customer> optionalCustomer = customerRepository.findByPhoneNumber(phone);
        if (optionalCustomer.isEmpty()) continue;

        Customer customer = optionalCustomer.get();
        List<EMISchedule> emiSchedules = emiScheduleRepository.findByCustomerPhoneOrderByMonth(phone);

        RecentPayerResponse response = new RecentPayerResponse();
        response.setCustomer(customer);
        response.setEmiSchedules(emiSchedules);

        responseList.add(response);
    }

    return ResponseEntity.ok(responseList);
}


    
    
    
    
    @GetMapping("/customers/{phoneNumber}/emi-schedule")
    public ResponseEntity<List<EMIScheduleResponse>> getEMISchedule(@PathVariable String phoneNumber) {
        try {
            log.info("🔥 Fetching EMI schedule for customer: {}", phoneNumber);
            
            List<EMIScheduleResponse> schedules = emiScheduleService.getEMIScheduleWithCumulative(phoneNumber);
            
            log.info("✅ EMI schedule fetched successfully. Total months: {}", schedules.size());
            return ResponseEntity.ok(schedules);
            
        } catch (Exception e) {
            log.error("❌ Error fetching EMI schedule: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }
    
    @PutMapping("/emi-schedule/{scheduleId}/status")
    public ResponseEntity<String> updatePaymentStatus(
            @PathVariable Long scheduleId, 
            @RequestBody UpdateStatusRequest request) {
        try {
            log.info("🔥 Updating payment status for schedule ID: {}", scheduleId);
            
            emiScheduleService.updatePaymentStatus(
                scheduleId, 
                request.getStatus(), 
                request.getPaidDate(),
                request.getPaidAmount()
            );
            
            log.info("✅ Payment status updated successfully");
            return ResponseEntity.ok("Status updated successfully");
            
        } catch (Exception e) {
            log.error("❌ Error updating payment status: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error updating status: " + e.getMessage());
        }
    }
    
    
    
    
   
}

