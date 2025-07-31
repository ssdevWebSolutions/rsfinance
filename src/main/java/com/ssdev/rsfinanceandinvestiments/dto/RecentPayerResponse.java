package com.ssdev.rsfinanceandinvestiments.dto;

import java.util.List;

import com.ssdev.rsfinanceandinvestiments.entity.Customer;
import com.ssdev.rsfinanceandinvestiments.entity.EMISchedule;

import lombok.Data;

//RecentPayerResponse.java
@Data
public class RecentPayerResponse {
 private Customer customer;
 private List<EMISchedule> emiSchedules;
 public Customer getCustomer() {
	return customer;
 }
 public void setCustomer(Customer customer) {
	this.customer = customer;
 }
 public List<EMISchedule> getEmiSchedules() {
	return emiSchedules;
 }
 public void setEmiSchedules(List<EMISchedule> emiSchedules) {
	this.emiSchedules = emiSchedules;
 }
 
 
 
 
 
}

