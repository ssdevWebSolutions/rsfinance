package com.ssdev.rsfinanceandinvestiments.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

//EMISchedule.java
@Entity
@Table(name = "emi_schedules")
@Data
@Builder
public class EMISchedule {
 @Override
	public String toString() {
		return "EMISchedule [id=" + id + ", customerPhone=" + customerPhone + ", monthNumber=" + monthNumber
				+ ", monthName=" + monthName + ", emiAmount=" + emiAmount + ", dueDate=" + dueDate + ", status="
				+ status + ", paidDate=" + paidDate + ", paidAmount=" + paidAmount + ", pendingAmount=" + pendingAmount
				+ ", cumulativePending=" + cumulativePending + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
				+ "]";
	}

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 
 @Column(name = "customer_phone")
 private String customerPhone;
 
 @Column(name = "month_number")
 private Integer monthNumber;
 
 @Column(name = "month_name")
 private String monthName;
 
 @Column(name = "emi_amount")
 private BigDecimal emiAmount;
 
 @Column(name = "due_date")
 private LocalDate dueDate;
 
 @Enumerated(EnumType.STRING)
 private PaymentStatus status;
 
 @Column(name = "paid_date")
 private LocalDate paidDate;
 
 @Column(name = "paid_amount")
 private BigDecimal paidAmount = BigDecimal.ZERO;
 
 @Column(name = "pending_amount")
 private BigDecimal pendingAmount = BigDecimal.ZERO;
 
 @Column(name = "cumulative_pending") 
 private BigDecimal cumulativePending = BigDecimal.ZERO;
 
 @CreationTimestamp
 @Column(name = "created_at")
 private LocalDateTime createdAt;
 
 @UpdateTimestamp
 @Column(name = "updated_at")
 private LocalDateTime updatedAt;

 public Long getId() {
	return id;
 }

 public void setId(Long id) {
	this.id = id;
 }

 public String getCustomerPhone() {
	return customerPhone;
 }

 public void setCustomerPhone(String customerPhone) {
	this.customerPhone = customerPhone;
 }

 public Integer getMonthNumber() {
	return monthNumber;
 }

 public void setMonthNumber(Integer monthNumber) {
	this.monthNumber = monthNumber;
 }

 public String getMonthName() {
	return monthName;
 }

 public void setMonthName(String monthName) {
	this.monthName = monthName;
 }

 public BigDecimal getEmiAmount() {
	return emiAmount;
 }

 public void setEmiAmount(BigDecimal emiAmount) {
	this.emiAmount = emiAmount;
 }

 public LocalDate getDueDate() {
	return dueDate;
 }

 public void setDueDate(LocalDate dueDate) {
	this.dueDate = dueDate;
 }

 public PaymentStatus getStatus() {
	return status;
 }

 public void setStatus(PaymentStatus status) {
	this.status = status;
 }

 public LocalDate getPaidDate() {
	return paidDate;
 }

 public void setPaidDate(LocalDate paidDate) {
	this.paidDate = paidDate;
 }

 public BigDecimal getPaidAmount() {
	return paidAmount;
 }

 public void setPaidAmount(BigDecimal paidAmount) {
	this.paidAmount = paidAmount;
 }

 public BigDecimal getPendingAmount() {
	return pendingAmount;
 }

 public void setPendingAmount(BigDecimal pendingAmount) {
	this.pendingAmount = pendingAmount;
 }

 public BigDecimal getCumulativePending() {
	return cumulativePending;
 }

 public void setCumulativePending(BigDecimal cumulativePending) {
	this.cumulativePending = cumulativePending;
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

 public EMISchedule() {
	super();
	// TODO Auto-generated constructor stub
 }
 
 

 
 
 
 
 
}


