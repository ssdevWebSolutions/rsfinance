package com.ssdev.rsfinanceandinvestiments.controller;


import com.ssdev.rsfinanceandinvestiments.Enums.MonthCategory;
import com.ssdev.rsfinanceandinvestiments.dto.MonthlyAnalyticsResponse;
import com.ssdev.rsfinanceandinvestiments.dto.RecentPayerResponse;
import com.ssdev.rsfinanceandinvestiments.repository.CustomerRepository;
import com.ssdev.rsfinanceandinvestiments.repository.EMIScheduleRepository;
import com.ssdev.rsfinanceandinvestiments.service.AnalyticsService;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;
    
    @Autowired
    private EMIScheduleRepository emiScheduleRepository;
    
    @Autowired 
    private CustomerRepository customerRepository;

    @GetMapping("/monthly")
    public ResponseEntity<MonthlyAnalyticsResponse> getMonthlyAnalytics(
            @RequestParam("monthCategory") MonthCategory monthCategory,
            @RequestParam("year") int year) {
        try {
        	System.out.print("hello ->>>>>"+monthCategory);
            MonthlyAnalyticsResponse response = analyticsService.getMonthlyAnalytics(monthCategory, year);
//            System.out.println("hello"+response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
        	System.out.print(e);
            return ResponseEntity.status(502).body(null);
        }
    }
    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    
    
    

    // 1️⃣ All customers
    @GetMapping("/month")
    public List<RecentPayerResponse> getAllCustomers(
            @RequestParam MonthCategory monthCategory,
            @RequestParam int year) {
        log.info("API GET /month called with monthCategory={} year={}", monthCategory, year);
        return analyticsService.getAllCustomers(monthCategory, year);
    }

    // 2️⃣ Paid customers
    @GetMapping("/paid")
    public List<RecentPayerResponse> getPaidCustomers(
            @RequestParam MonthCategory monthCategory,
            @RequestParam int year) {
        log.info("API GET /paid called with monthCategory={} year={}", monthCategory, year);
        return analyticsService.getPaidCustomers(monthCategory, year);
    }

    // 3️⃣ Pending customers
    @GetMapping("/pending")
    public List<RecentPayerResponse> getPendingCustomers(
            @RequestParam MonthCategory monthCategory,
            @RequestParam int year) {
        log.info("API GET /pending called with monthCategory={} year={}", monthCategory, year);
        return analyticsService.getPendingCustomers(monthCategory, year);
    }

    // 4️⃣ Waitlist customers (no month/year filter needed)
//    @GetMapping("/waitlist")
//    public List<RecentPayerResponse> getWaitlistCustomers() {
//        log.info("API GET /waitlist called");
//        return analyticsService.getWaitlistCustomers();
//    }
    
//    @GetMapping("/waitlist")
//    public List<RecentPayerResponse> getWaitlistCustomers() {
//        LocalDate cutoff = LocalDate.now().minusMonths(3);
//        log.info("Fetching WAITLIST customers pending since {}", cutoff);
//
//        List<String> waitlistPhones = emiScheduleRepository.findWaitlistCustomers(cutoff);
//
//        return waitlistPhones.stream()
//                .map(phone -> {
//                    RecentPayerResponse dto = new RecentPayerResponse();
//                    dto.setCustomer(customerRepository.findByPhoneNumber(phone).orElse(null));
//                    dto.setEmiSchedules(
//                            emiScheduleRepository.findByCustomerPhoneOrderByMonth(phone)
//                    );
//                    return dto;
//                })
//                .toList();
//    }
    
    @GetMapping("/waitlist")
    public List<RecentPayerResponse> getWaitlistCustomers() {
        LocalDate today = LocalDate.now();
        log.info("Fetching WAITLIST customers with 3+ pending as of {}", today);

        List<String> waitlistPhones = emiScheduleRepository.findWaitlistCustomers(today);
        log.info("Waitlist phones: {}", waitlistPhones);

        return waitlistPhones.stream()
                .map(phone -> {
                    RecentPayerResponse dto = new RecentPayerResponse();
                    dto.setCustomer(customerRepository.findByPhoneNumber(phone).orElse(null));
                    dto.setEmiSchedules(
                            emiScheduleRepository.findByCustomerPhoneOrderByMonth(phone)
                    );
                    return dto;
                })
                .toList();
    }



}

