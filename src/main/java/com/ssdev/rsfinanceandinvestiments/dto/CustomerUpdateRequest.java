package com.ssdev.rsfinanceandinvestiments.dto;


import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class CustomerUpdateRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "Place is required")
    @Size(min = 2, max = 100, message = "Place must be between 2 and 100 characters")
    private String place;
    
    @NotBlank(message = "Refer by is required")
    @Size(min = 2, max = 100, message = "Refer by must be between 2 and 100 characters")
    private String referBy;
    
    @NotBlank(message = "Job is required")
    @Size(min = 2, max = 100, message = "Job must be between 2 and 100 characters")
    private String job;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phoneNumber;
    
    @NotNull(message = "Amount taken is required")
    @DecimalMin(value = "1000.0", message = "Amount taken must be at least ₹1000")
    @DecimalMax(value = "10000000.0", message = "Amount taken cannot exceed ₹1 crore")
    private BigDecimal amountTaken;
    
    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.0", message = "Interest rate cannot be negative")
    @DecimalMax(value = "50.0", message = "Interest rate cannot exceed 50%")
    private BigDecimal interest;
    
    @NotNull(message = "Tenure is required")
    @Min(value = 1, message = "Tenure must be at least 1 month")
    @Max(value = 360, message = "Tenure cannot exceed 360 months")
    private BigDecimal tenure;
    
    @NotNull(message = "Monthly EMI is required")
    @DecimalMin(value = "100.0", message = "Monthly EMI must be at least ₹100")
    private BigDecimal monthlyEmi;
    
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "1000.0", message = "Total amount must be at least ₹1000")
    private BigDecimal totalAmount;
    
    @NotBlank(message = "Start date is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Start date must be in YYYY-MM-DD format")
    private String startDate;
    
    @NotBlank(message = "End date is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "End date must be in YYYY-MM-DD format")
    private String endDate;
    
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

	public BigDecimal getTenure() {
		return tenure;
	}

	public void setTenure(BigDecimal tenure) {
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

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public CustomerUpdateRequest(
			@NotBlank(message = "Name is required") @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters") String name,
			@NotBlank(message = "Place is required") @Size(min = 2, max = 100, message = "Place must be between 2 and 100 characters") String place,
			@NotBlank(message = "Refer by is required") @Size(min = 2, max = 100, message = "Refer by must be between 2 and 100 characters") String referBy,
			@NotBlank(message = "Job is required") @Size(min = 2, max = 100, message = "Job must be between 2 and 100 characters") String job,
			@NotBlank(message = "Phone number is required") @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits") String phoneNumber,
			@NotNull(message = "Amount taken is required") @DecimalMin(value = "1000.0", message = "Amount taken must be at least ₹1000") @DecimalMax(value = "10000000.0", message = "Amount taken cannot exceed ₹1 crore") BigDecimal amountTaken,
			@NotNull(message = "Interest rate is required") @DecimalMin(value = "0.0", message = "Interest rate cannot be negative") @DecimalMax(value = "50.0", message = "Interest rate cannot exceed 50%") BigDecimal interest,
			@NotNull(message = "Tenure is required") @Min(value = 1, message = "Tenure must be at least 1 month") @Max(value = 360, message = "Tenure cannot exceed 360 months") BigDecimal tenure,
			@NotNull(message = "Monthly EMI is required") @DecimalMin(value = "100.0", message = "Monthly EMI must be at least ₹100") BigDecimal monthlyEmi,
			@NotNull(message = "Total amount is required") @DecimalMin(value = "1000.0", message = "Total amount must be at least ₹1000") BigDecimal totalAmount,
			@NotBlank(message = "Start date is required") @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Start date must be in YYYY-MM-DD format") String startDate,
			@NotBlank(message = "End date is required") @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "End date must be in YYYY-MM-DD format") String endDate) {
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

	public CustomerUpdateRequest() {
		super();
		// TODO Auto-generated constructor stub
	}

	// 🔥 VALIDATION: Custom validation method (optional)
    public boolean isValid() {
        // Additional custom validation logic can be added here
        return true;
    }
    
 // 🔥 HELPER: Calculate monthly EMI
    public void calculateAndSetEMI() {
        if (amountTaken != null && interest != null && tenure != null &&
            amountTaken.compareTo(BigDecimal.ZERO) > 0 &&
            interest.compareTo(BigDecimal.ZERO) >= 0 &&
            tenure.compareTo(BigDecimal.ZERO) > 0) {

            // Convert annual interest rate to monthly interest rate
            BigDecimal monthlyRate = interest.divide(BigDecimal.valueOf(12 * 100), 10, RoundingMode.HALF_UP);

            BigDecimal emi;

            if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
                emi = amountTaken.divide(tenure, 2, RoundingMode.HALF_UP);
            } else {
                // EMI = [P x R x (1+R)^N] / [(1+R)^N - 1]
                BigDecimal onePlusR = monthlyRate.add(BigDecimal.ONE);
                BigDecimal onePlusRPowerN = onePlusR.pow(tenure.intValue());
                BigDecimal numerator = amountTaken.multiply(monthlyRate).multiply(onePlusRPowerN);
                BigDecimal denominator = onePlusRPowerN.subtract(BigDecimal.ONE);
                emi = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
            }

            this.monthlyEmi = emi;
            this.totalAmount = emi.multiply(tenure).setScale(2, RoundingMode.HALF_UP);
        }
    }

    
    @Override
    public String toString() {
        return "CustomerUpdateRequest{" +
                "name='" + name + '\'' +
                ", place='" + place + '\'' +
                ", referBy='" + referBy + '\'' +
                ", job='" + job + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", amountTaken=" + amountTaken +
                ", interest=" + interest +
                ", tenure=" + tenure +
                ", monthlyEmi=" + monthlyEmi +
                ", totalAmount=" + totalAmount +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                '}';
    }
}