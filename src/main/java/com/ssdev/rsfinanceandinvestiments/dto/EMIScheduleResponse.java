package com.ssdev.rsfinanceandinvestiments.dto;

import java.math.BigDecimal;

import lombok.Data;

//EMIScheduleResponse.java
@Data
public class EMIScheduleResponse {
 private Long id;
 private String customerPhone;
 private Integer monthNumber;
 private String monthName;
 private BigDecimal emiAmount;
 private String dueDate;
 private String status;
 private String paidDate;
 private BigDecimal paidAmount;
 private BigDecimal pendingAmount;
 private BigDecimal cumulativePending; // 🔥 This shows total pending up to this month
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
 public String getDueDate() {
	return dueDate;
 }
 public void setDueDate(String dueDate) {
	this.dueDate = dueDate;
 }
 public String getStatus() {
	return status;
 }
 public void setStatus(String status) {
	this.status = status;
 }
 public String getPaidDate() {
	return paidDate;
 }
 public void setPaidDate(String paidDate) {
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
 public EMIScheduleResponse(Long id, String customerPhone, Integer monthNumber, String monthName, BigDecimal emiAmount,
		String dueDate, String status, String paidDate, BigDecimal paidAmount, BigDecimal pendingAmount,
		BigDecimal cumulativePending) {
	super();
	this.id = id;
	this.customerPhone = customerPhone;
	this.monthNumber = monthNumber;
	this.monthName = monthName;
	this.emiAmount = emiAmount;
	this.dueDate = dueDate;
	this.status = status;
	this.paidDate = paidDate;
	this.paidAmount = paidAmount;
	this.pendingAmount = pendingAmount;
	this.cumulativePending = cumulativePending;
 }
 @Override
 public String toString() {
	return "EMIScheduleResponse [id=" + id + ", customerPhone=" + customerPhone + ", monthNumber=" + monthNumber
			+ ", monthName=" + monthName + ", emiAmount=" + emiAmount + ", dueDate=" + dueDate + ", status=" + status
			+ ", paidDate=" + paidDate + ", paidAmount=" + paidAmount + ", pendingAmount=" + pendingAmount
			+ ", cumulativePending=" + cumulativePending + "]";
 }
 public EMIScheduleResponse() {
	super();
	// TODO Auto-generated constructor stub
 }
 
 
 
 
 
}
