package com.ssdev.rsfinanceandinvestiments.controller;


import com.ssdev.rsfinanceandinvestiments.Enums.MonthCategory;
import com.ssdev.rsfinanceandinvestiments.dto.MonthlyAnalyticsResponse;
import com.ssdev.rsfinanceandinvestiments.service.AnalyticsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/monthly")
    public ResponseEntity<MonthlyAnalyticsResponse> getMonthlyAnalytics(
            @RequestParam("monthCategory") MonthCategory monthCategory,
            @RequestParam("year") int year) {
        try {
        	System.out.print("hello");
            MonthlyAnalyticsResponse response = analyticsService.getMonthlyAnalytics(monthCategory, year);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}

