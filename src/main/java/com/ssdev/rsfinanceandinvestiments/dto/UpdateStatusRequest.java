package com.ssdev.rsfinanceandinvestiments.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class UpdateStatusRequest {
 private String status;
 private String paidDate;
 private BigDecimal paidAmount;
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
 public UpdateStatusRequest() {
	super();
	// TODO Auto-generated constructor stub
 }
 public UpdateStatusRequest(String status, String paidDate, BigDecimal paidAmount) {
	super();
	this.status = status;
	this.paidDate = paidDate;
	this.paidAmount = paidAmount;
 }
 @Override
 public String toString() {
	return "UpdateStatusRequest [status=" + status + ", paidDate=" + paidDate + ", paidAmount=" + paidAmount + "]";
 }
 
 
 
 
 
 
 
 
}
