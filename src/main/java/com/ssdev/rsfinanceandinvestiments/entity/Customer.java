package com.ssdev.rsfinanceandinvestiments.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Builder;


import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Builder
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;
    
    @NotBlank(message = "Place is required")
    @Size(max = 100, message = "Place must be less than 100 characters")
    @Column(nullable = false, length = 100)
    private String place;
    
    @Size(max = 100, message = "Refer by must be less than 100 characters")
    @Column(name = "refer_by", length = 100)
    private String referBy;
    
    @NotBlank(message = "Job is required")
    @Size(max = 100, message = "Job must be less than 100 characters")
    @Column(nullable = false, length = 100)
    private String job;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian phone number")
    @Column(name = "phone_number", nullable = false, unique = true, length = 15)
    private String phoneNumber;
    
    @NotNull(message = "Amount taken is required")
    @DecimalMin(value = "1000.0", message = "Amount taken must be at least ₹1000")
    @DecimalMax(value = "10000000.0", message = "Amount taken must be less than ₹10,00,000")
    @Column(name = "amount_taken", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountTaken;
    
    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.1", message = "Interest rate must be at least 0.1%")
    @DecimalMax(value = "50.0", message = "Interest rate must be less than 50%")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interest;
    
    @NotNull(message = "Tenure is required")
    @Min(value = 1, message = "Tenure must be at least 1 month")
    @Max(value = 360, message = "Tenure must be less than 360 months")
    @Column(nullable = false)
    private Integer tenure;
    
    @NotNull(message = "Monthly EMI is required")
    @DecimalMin(value = "100.0", message = "Monthly EMI must be at least ₹100")
    @Column(name = "monthly_emi", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyEmi;
    
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "1000.0", message = "Total amount must be at least ₹1000")
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;
    
    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private CustomerStatus status = CustomerStatus.ACTIVE;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Enum for customer status
    public enum CustomerStatus {
        ACTIVE, INACTIVE, COMPLETED, DEFAULTED
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

	public CustomerStatus getStatus() {
		return status;
	}

	public void setStatus(CustomerStatus status) {
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

	public Customer() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Customer(Long id,
			@NotBlank(message = "Name is required") @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters") String name,
			@NotBlank(message = "Place is required") @Size(max = 100, message = "Place must be less than 100 characters") String place,
			@Size(max = 100, message = "Refer by must be less than 100 characters") String referBy,
			@NotBlank(message = "Job is required") @Size(max = 100, message = "Job must be less than 100 characters") String job,
			@NotBlank(message = "Phone number is required") @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian phone number") String phoneNumber,
			@NotNull(message = "Amount taken is required") @DecimalMin(value = "1000.0", message = "Amount taken must be at least ₹1000") @DecimalMax(value = "10000000.0", message = "Amount taken must be less than ₹10,00,000") BigDecimal amountTaken,
			@NotNull(message = "Interest rate is required") @DecimalMin(value = "0.1", message = "Interest rate must be at least 0.1%") @DecimalMax(value = "50.0", message = "Interest rate must be less than 50%") BigDecimal interest,
			@NotNull(message = "Tenure is required") @Min(value = 1, message = "Tenure must be at least 1 month") @Max(value = 360, message = "Tenure must be less than 360 months") Integer tenure,
			@NotNull(message = "Monthly EMI is required") @DecimalMin(value = "100.0", message = "Monthly EMI must be at least ₹100") BigDecimal monthlyEmi,
			@NotNull(message = "Total amount is required") @DecimalMin(value = "1000.0", message = "Total amount must be at least ₹1000") BigDecimal totalAmount,
			@NotNull(message = "Start date is required") LocalDate startDate,
			@NotNull(message = "End date is required") LocalDate endDate, CustomerStatus status,
			LocalDateTime createdAt, LocalDateTime updatedAt) {
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

	@Override
	public String toString() {
		return "Customer [id=" + id + ", name=" + name + ", place=" + place + ", referBy=" + referBy + ", job=" + job
				+ ", phoneNumber=" + phoneNumber + ", amountTaken=" + amountTaken + ", interest=" + interest
				+ ", tenure=" + tenure + ", monthlyEmi=" + monthlyEmi + ", totalAmount=" + totalAmount + ", startDate="
				+ startDate + ", endDate=" + endDate + ", status=" + status + ", createdAt=" + createdAt
				+ ", updatedAt=" + updatedAt + "]";
	}
    
    
    
    
    
}
