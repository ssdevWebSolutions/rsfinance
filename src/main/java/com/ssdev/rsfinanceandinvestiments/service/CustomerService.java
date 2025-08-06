package com.ssdev.rsfinanceandinvestiments.service;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ssdev.rsfinanceandinvestiments.dto.CustomerRequest;
import com.ssdev.rsfinanceandinvestiments.dto.CustomerResponse;
import com.ssdev.rsfinanceandinvestiments.dto.CustomerUpdateRequest;
import com.ssdev.rsfinanceandinvestiments.entity.Customer;
import com.ssdev.rsfinanceandinvestiments.repository.CustomerRepository;
import com.ssdev.rsfinanceandinvestiments.repository.EMIScheduleRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CustomerService {
	
	@Autowired
	private  CustomerRepository customerRepository;
	
	@Autowired
	private EMIScheduleRepository emiScheduleRepository;
	
	 private static final Logger log = LoggerFactory.getLogger(EMIScheduleService.class);

	
	public Customer createCustomer(Customer request) {
       
		return customerRepository.save(request);
    }
	
	
	public List<Customer> findAll()
	{
		return customerRepository.findAll();
	}
	
	
	@Transactional
public Customer updateCustomer(String phoneNumber, CustomerUpdateRequest request) {
    try {
        log.info("🔥 Updating customer with phone number: {}", phoneNumber);

        // 1. Fetch the existing customer
        Customer existingCustomer = customerRepository.findByPhoneNumber(phoneNumber)
            .orElseThrow(() -> new RuntimeException("Customer not found with phone number: " + phoneNumber));

        log.info("🔍 Found existing customer: {}", existingCustomer.getName());

        // 2. Calculate EMI and Total Amount
        request.calculateAndSetEMI(); // calculate and update EMI + totalAmount inside the DTO

        // 3. Parse LocalDate (not java.util.Date!)
        LocalDate startDate = LocalDate.parse(request.getStartDate());
        LocalDate endDate = LocalDate.parse(request.getEndDate());

        // 4. Update fields
        existingCustomer.setName(request.getName());
        existingCustomer.setPlace(request.getPlace());
        existingCustomer.setReferBy(request.getReferBy());
        existingCustomer.setJob(request.getJob());
        existingCustomer.setPhoneNumber(request.getPhoneNumber());
        existingCustomer.setAmountTaken(request.getAmountTaken());
        existingCustomer.setInterest(request.getInterest());
        existingCustomer.setTenure(request.getTenure().intValue()); // Convert BigDecimal -> Integer
        existingCustomer.setMonthlyEmi(request.getMonthlyEmi());
        existingCustomer.setTotalAmount(request.getTotalAmount());
        existingCustomer.setStartDate(startDate);
        existingCustomer.setEndDate(endDate);

        // 5. Save the updated customer
        Customer updatedCustomer = customerRepository.save(existingCustomer);

        log.info("✅ Customer updated successfully: {}", updatedCustomer.getName());
        log.info("📊 Updated EMI: ₹{}, Total Amount: ₹{}", request.getMonthlyEmi(), request.getTotalAmount());

        return updatedCustomer;

    } catch (Exception e) {
        log.error("❌ Error updating customer: {}", e.getMessage(), e);
        throw new RuntimeException("Failed to update customer: " + e.getMessage());
    }
}


    // 🔥 NEW: DELETE CUSTOMER
    @Transactional
    public void deleteCustomer(String phoneNumber) {
        try {
            log.info("🔥 Deleting customer with phone number: {}", phoneNumber);
            
            // Check if customer exists
            Optional<Customer> optionalCustomer = customerRepository.findByPhoneNumber(phoneNumber);
            if (optionalCustomer.isEmpty()) {
                log.error("❌ Customer not found with phone number: {}", phoneNumber);
                throw new RuntimeException("Customer not found with phone number: " + phoneNumber);
            }

            Customer customer = optionalCustomer.get();
            log.info("🔍 Found customer to delete: {}", customer.getName());

            // Delete EMI schedules first (if not configured for cascade delete)
            log.info("🗑️ Deleting EMI schedules for customer: {}", phoneNumber);
            emiScheduleRepository.deleteByCustomerPhone(phoneNumber);
            
            // Delete customer
            customerRepository.deleteByPhoneNumber(phoneNumber);
            
            log.info("✅ Customer and associated EMI schedules deleted successfully: {}", phoneNumber);

        } catch (Exception e) {
            log.error("❌ Error deleting customer: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete customer: " + e.getMessage());
        }
    }

    // 🔥 NEW: GET CUSTOMER BY PHONE NUMBER
    public Optional<Customer> findByPhoneNumber(String phoneNumber) {
        try {
            log.info("🔍 Finding customer by phone number: {}", phoneNumber);
            Optional<Customer> customer = customerRepository.findByPhoneNumber(phoneNumber);
            
            if (customer.isPresent()) {
                log.info("✅ Customer found: {}", customer.get().getName());
            } else {
                log.info("❌ Customer not found with phone number: {}", phoneNumber);
            }
            
            return customer;
        } catch (Exception e) {
            log.error("❌ Error finding customer by phone number: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to find customer: " + e.getMessage());
        }
    }

    // 🔥 NEW: CHECK IF CUSTOMER EXISTS
//    public boolean existsByPhoneNumber(String phoneNumber) {
//        try {
//            boolean exists = customerRepository.existsByPhoneNumber(phoneNumber);
//            log.info("🔍 Customer exists check for {}: {}", phoneNumber, exists);
//            return exists;
//        } catch (Exception e) {
//            log.error("❌ Error checking customer existence: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to check customer existence: " + e.getMessage());
//        }
//    }

    // 🔥 HELPER: Calculate EMI using formula
    private double calculateEMI(double principal, double annualRate, int tenureMonths) {
        try {
            log.info("📊 Calculating EMI - Principal: ₹{}, Rate: {}%, Tenure: {} months", 
                     principal, annualRate, tenureMonths);

            if (annualRate == 0) {
                // Simple division if no interest
                double emi = principal / tenureMonths;
                log.info("✅ EMI calculated (no interest): ₹{}", emi);
                return emi;
            }

            // Convert annual rate to monthly rate
            double monthlyRate = annualRate / (12 * 100);
            
            // EMI formula: P * r * (1+r)^n / ((1+r)^n - 1)
            double emi = (principal * monthlyRate * Math.pow(1 + monthlyRate, tenureMonths)) / 
                         (Math.pow(1 + monthlyRate, tenureMonths) - 1);
            
            log.info("✅ EMI calculated: ₹{}", emi);
            return Math.round(emi * 100.0) / 100.0; // Round to 2 decimal places

        } catch (Exception e) {
            log.error("❌ Error calculating EMI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to calculate EMI: " + e.getMessage());
        }
    }

    // 🔥 HELPER: Calculate total amount
    private double calculateTotalAmount(double principal, double annualRate, int tenureMonths) {
        try {
            double emi = calculateEMI(principal, annualRate, tenureMonths);
            double totalAmount = emi * tenureMonths;
            
            log.info("📊 Total amount calculated: ₹{}", totalAmount);
            return Math.round(totalAmount * 100.0) / 100.0; // Round to 2 decimal places

        } catch (Exception e) {
            log.error("❌ Error calculating total amount: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to calculate total amount: " + e.getMessage());
        }
    }

}
