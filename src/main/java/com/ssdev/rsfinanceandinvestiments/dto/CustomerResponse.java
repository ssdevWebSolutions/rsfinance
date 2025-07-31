package com.ssdev.rsfinanceandinvestiments.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.ssdev.rsfinanceandinvestiments.entity.Customer;
import com.ssdev.rsfinanceandinvestiments.entity.Customer.CustomerStatus;

@Data
@Builder
public class CustomerResponse {
    private Long id;
    private String name;
    private String place;
    private String referBy;
    private String job;
    private String phoneNumber;
    private BigDecimal amountTaken;
    private BigDecimal interest;
    private Integer tenure;
    private BigDecimal monthlyEmi;
    private BigDecimal totalAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Customer.CustomerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
	public CustomerResponse() {
		super();
		// TODO Auto-generated constructor stub
	}
	public CustomerResponse(Long id, String name, String place, String referBy, String job, String phoneNumber,
			BigDecimal amountTaken, BigDecimal interest, Integer tenure, BigDecimal monthlyEmi, BigDecimal totalAmount,
			LocalDate startDate, LocalDate endDate, CustomerStatus status, LocalDateTime createdAt,
			LocalDateTime updatedAt) {
		super();
		this.id = id;
		this.name = name;
		this.place = place;
		this.referBy = referBy;
		this.job = job;
		this.phoneNumber = phoneNumber;
		this.amountTaken = amountTaken;
		this.interest = interest;
		this.tenure = tenure;
		this.monthlyEmi = monthlyEmi;
		this.totalAmount = totalAmount;
		this.startDate = startDate;
		this.endDate = endDate;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPlace() {
		return place;
	}
	public void setPlace(String place) {
		this.place = place;
	}
	public String getReferBy() {
		return referBy;
	}
	public void setReferBy(String referBy) {
		this.referBy = referBy;
	}
	public String getJob() {
		return job;
	}
	public void setJob(String job) {
		this.job = job;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public BigDecimal getAmountTaken() {
		return amountTaken;
	}
	public void setAmountTaken(BigDecimal amountTaken) {
		this.amountTaken = amountTaken;
	}
	public BigDecimal getInterest() {
		return interest;
	}
	public void setInterest(BigDecimal interest) {
		this.interest = interest;
	}
	public Integer getTenure() {
		return tenure;
	}
	public void setTenure(Integer tenure) {
		this.tenure = tenure;
	}
	public BigDecimal getMonthlyEmi() {
		return monthlyEmi;
	}
	public void setMonthlyEmi(BigDecimal monthlyEmi) {
		this.monthlyEmi = monthlyEmi;
	}
	public BigDecimal getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}
	public LocalDate getStartDate() {
		return startDate;
	}
	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}
	public LocalDate getEndDate() {
		return endDate;
	}
	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}
	public Customer.CustomerStatus getStatus() {
		return status;
	}
	public void setStatus(Customer.CustomerStatus status) {
		this.status = status;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
	@Override
	public String toString() {
		return "CustomerResponse [id=" + id + ", name=" + name + ", place=" + place + ", referBy=" + referBy + ", job="
				+ job + ", phoneNumber=" + phoneNumber + ", amountTaken=" + amountTaken + ", interest=" + interest
				+ ", tenure=" + tenure + ", monthlyEmi=" + monthlyEmi + ", totalAmount=" + totalAmount + ", startDate="
				+ startDate + ", endDate=" + endDate + ", status=" + status + ", createdAt=" + createdAt
				+ ", updatedAt=" + updatedAt + "]";
	}
    
    
    
    
    
    
    
}
