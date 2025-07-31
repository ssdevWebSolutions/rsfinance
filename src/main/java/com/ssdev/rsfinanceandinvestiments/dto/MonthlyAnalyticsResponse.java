package com.ssdev.rsfinanceandinvestiments.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonthlyAnalyticsResponse {
    private String month;
    private int year;

    private int totalCustomers;
    private int paidCustomers;
    private int pendingCustomers;
    private int waitlistCustomers;
    
    
    private double customerGrowthPercentage;
    private double paidPercentage;
    private double pendingPercentage;
    private double waitlistPercentage;
    private double collectionPercentage;

    
    private String customerGrowthDirection; // "+" or "-"
    private String paidDirection;
    private String pendingDirection;
    private String waitlistDirection;
    private String collectionDirection;

    public String getCustomerGrowthDirection() {
		return customerGrowthDirection;
	}

	public void setCustomerGrowthDirection(String customerGrowthDirection) {
		this.customerGrowthDirection = customerGrowthDirection;
	}

	public String getPaidDirection() {
		return paidDirection;
	}

	public void setPaidDirection(String paidDirection) {
		this.paidDirection = paidDirection;
	}

	public String getPendingDirection() {
		return pendingDirection;
	}

	public void setPendingDirection(String pendingDirection) {
		this.pendingDirection = pendingDirection;
	}

	public String getWaitlistDirection() {
		return waitlistDirection;
	}

	public void setWaitlistDirection(String waitlistDirection) {
		this.waitlistDirection = waitlistDirection;
	}

	public String getCollectionDirection() {
		return collectionDirection;
	}

	public void setCollectionDirection(String collectionDirection) {
		this.collectionDirection = collectionDirection;
	}

	public double getCustomerGrowthPercentage() {
		return customerGrowthPercentage;
	}

	public void setCustomerGrowthPercentage(double customerGrowthPercentage) {
		this.customerGrowthPercentage = customerGrowthPercentage;
	}

	public double getPaidPercentage() {
		return paidPercentage;
	}

	public void setPaidPercentage(double paidPercentage) {
		this.paidPercentage = paidPercentage;
	}

	public double getPendingPercentage() {
		return pendingPercentage;
	}

	public void setPendingPercentage(double pendingPercentage) {
		this.pendingPercentage = pendingPercentage;
	}

	public double getWaitlistPercentage() {
		return waitlistPercentage;
	}

	public void setWaitlistPercentage(double waitlistPercentage) {
		this.waitlistPercentage = waitlistPercentage;
	}

	public double getCollectionPercentage() {
		return collectionPercentage;
	}

	public void setCollectionPercentage(double collectionPercentage) {
		this.collectionPercentage = collectionPercentage;
	}

	private BigDecimal totalCollectedAmount;

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getTotalCustomers() {
		return totalCustomers;
	}

	public void setTotalCustomers(int totalCustomers) {
		this.totalCustomers = totalCustomers;
	}

	public int getPaidCustomers() {
		return paidCustomers;
	}

	public void setPaidCustomers(int paidCustomers) {
		this.paidCustomers = paidCustomers;
	}

	public int getPendingCustomers() {
		return pendingCustomers;
	}

	public void setPendingCustomers(int pendingCustomers) {
		this.pendingCustomers = pendingCustomers;
	}

	public int getWaitlistCustomers() {
		return waitlistCustomers;
	}

	public void setWaitlistCustomers(int waitlistCustomers) {
		this.waitlistCustomers = waitlistCustomers;
	}

	public BigDecimal getTotalCollectedAmount() {
		return totalCollectedAmount;
	}

	public void setTotalCollectedAmount(BigDecimal totalCollectedAmount) {
		this.totalCollectedAmount = totalCollectedAmount;
	}
    
    
    
    
}

