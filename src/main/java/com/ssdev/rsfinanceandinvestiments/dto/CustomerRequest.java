package com.ssdev.rsfinanceandinvestiments.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class CustomerRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "Place is required")
    @Size(max = 100, message = "Place must be less than 100 characters")
    private String place;
    
    @Size(max = 100, message = "Refer by must be less than 100 characters")
    private String referBy;
    
    @NotBlank(message = "Job is required")
    @Size(max = 100, message = "Job must be less than 100 characters")
    private String job;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian phone number format")
    private String phoneNumber;
    
    @NotNull(message = "Amount taken is required")
    @DecimalMin(value = "1000.0", message = "Amount taken must be at least ₹1000")
    @DecimalMax(value = "10000000.0", message = "Amount taken cannot exceed ₹1,00,00,000")
    private BigDecimal amountTaken;
    
    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.1", message = "Interest rate must be at least 0.1%")
    @DecimalMax(value = "50.0", message = "Interest rate cannot exceed 50%")
    private BigDecimal interest;
    
    @NotNull(message = "Tenure is required")
    @Min(value = 1, message = "Tenure must be at least 1 month")
    @Max(value = 360, message = "Tenure cannot exceed 360 months")
    private Integer tenure;
    
    @NotNull(message = "Monthly EMI is required")
    @DecimalMin(value = "100.0", message = "Monthly EMI must be at least ₹100")
    private BigDecimal monthlyEmi;
    
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "1000.0", message = "Total amount must be at least ₹1000")
    private BigDecimal totalAmount;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;

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

	public CustomerRequest() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CustomerRequest(
			@NotBlank(message = "Name is required") @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters") String name,
			@NotBlank(message = "Place is required") @Size(max = 100, message = "Place must be less than 100 characters") String place,
			@Size(max = 100, message = "Refer by must be less than 100 characters") String referBy,
			@NotBlank(message = "Job is required") @Size(max = 100, message = "Job must be less than 100 characters") String job,
			@NotBlank(message = "Phone number is required") @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian phone number format") String phoneNumber,
			@NotNull(message = "Amount taken is required") @DecimalMin(value = "1000.0", message = "Amount taken must be at least ₹1000") @DecimalMax(value = "10000000.0", message = "Amount taken cannot exceed ₹1,00,00,000") BigDecimal amountTaken,
			@NotNull(message = "Interest rate is required") @DecimalMin(value = "0.1", message = "Interest rate must be at least 0.1%") @DecimalMax(value = "50.0", message = "Interest rate cannot exceed 50%") BigDecimal interest,
			@NotNull(message = "Tenure is required") @Min(value = 1, message = "Tenure must be at least 1 month") @Max(value = 360, message = "Tenure cannot exceed 360 months") Integer tenure,
			@NotNull(message = "Monthly EMI is required") @DecimalMin(value = "100.0", message = "Monthly EMI must be at least ₹100") BigDecimal monthlyEmi,
			@NotNull(message = "Total amount is required") @DecimalMin(value = "1000.0", message = "Total amount must be at least ₹1000") BigDecimal totalAmount,
			@NotNull(message = "Start date is required") LocalDate startDate,
			@NotNull(message = "End date is required") LocalDate endDate) {
		super();
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
	}

	@Override
	public String toString() {
		return "CustomerRequest [name=" + name + ", place=" + place + ", referBy=" + referBy + ", job=" + job
				+ ", phoneNumber=" + phoneNumber + ", amountTaken=" + amountTaken + ", interest=" + interest
				+ ", tenure=" + tenure + ", monthlyEmi=" + monthlyEmi + ", totalAmount=" + totalAmount + ", startDate="
				+ startDate + ", endDate=" + endDate + "]";
	}
    
    
    
    
    
}