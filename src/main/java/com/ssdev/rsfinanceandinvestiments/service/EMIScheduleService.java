package com.ssdev.rsfinanceandinvestiments.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ssdev.rsfinanceandinvestiments.dto.EMIScheduleResponse;
import com.ssdev.rsfinanceandinvestiments.entity.Customer;
import com.ssdev.rsfinanceandinvestiments.entity.EMISchedule;
import com.ssdev.rsfinanceandinvestiments.entity.PaymentStatus;
import com.ssdev.rsfinanceandinvestiments.repository.EMIScheduleRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class EMIScheduleService {

	@Autowired
	private EMIScheduleRepository emiScheduleRepository;

	private static final Logger log = LoggerFactory.getLogger(EMIScheduleService.class);

	/**
	 * Generate EMI schedule when customer is created
	 */
	@Async
	public void generateEMIScheduleForCustomer(Customer customer) {
		log.info("🔥 Generating EMI schedule for customer: {}", customer.getPhoneNumber());

		LocalDate startDate = customer.getStartDate();

		for (int month = 1; month <= customer.getTenure(); month++) {
			LocalDate dueDate = startDate.plusMonths(month - 1);

			EMISchedule schedule = new EMISchedule();
			schedule.setCustomerPhone(customer.getPhoneNumber());
			schedule.setMonthNumber(month);
			schedule.setMonthName(getMonthName(dueDate));
			schedule.setEmiAmount(customer.getMonthlyEmi());
			schedule.setDueDate(dueDate);
			schedule.setStatus(PaymentStatus.PENDING);
			schedule.setPaidAmount(BigDecimal.ZERO);
			schedule.setPendingAmount(customer.getMonthlyEmi());
			schedule.setCumulativePending(BigDecimal.ZERO);

			emiScheduleRepository.save(schedule);
		}

		// 🔥 Calculate cumulative pending for all months
		calculateCumulativePending(customer.getPhoneNumber());

		log.info("✅ EMI schedule generated successfully for customer: {}", customer.getPhoneNumber());
	}

	/**
	 * 🔥 CORE LOGIC: Calculate cumulative pending amounts
	 */
	public void calculateCumulativePending(String customerPhone) {
		List<EMISchedule> schedules = emiScheduleRepository.findByCustomerPhoneOrderByMonth(customerPhone);
		BigDecimal runningPending = BigDecimal.ZERO;

		for (EMISchedule schedule : schedules) {
			// Add current month's pending to running total
			if (schedule.getStatus() != PaymentStatus.PAID) {
				runningPending = runningPending.add(schedule.getPendingAmount());
			}

			// Set cumulative pending for this month
			schedule.setCumulativePending(runningPending);

			// FIXED: Update status based on 3-month rule
			updateStatusBasedOnThreeMonthRule(schedule);

			emiScheduleRepository.save(schedule);

			log.debug("Month {}: EMI=₹{}, Pending=₹{}, Cumulative=₹{}, Status={}", schedule.getMonthNumber(),
					schedule.getEmiAmount(), schedule.getPendingAmount(), schedule.getCumulativePending(),
					schedule.getStatus());
		}

		log.info("✅ Cumulative pending calculation completed for customer: {}", customerPhone);
	}

	/**
	 * Update payment status for a specific EMI
	 */
	public void updatePaymentStatus(Long scheduleId, String status, String paidDateStr, BigDecimal paidAmount) {
		log.info("🔥 Updating payment status for schedule ID: {}", scheduleId);

		Optional<EMISchedule> optionalSchedule = emiScheduleRepository.findById(scheduleId);
		if (optionalSchedule.isEmpty()) {
			throw new RuntimeException("EMI Schedule not found with ID: " + scheduleId);
		}

		EMISchedule schedule = optionalSchedule.get();
		PaymentStatus newStatus = PaymentStatus.valueOf(status.toUpperCase());

		// --- 1️⃣ Update the requested EMI's payment details ---
		schedule.setStatus(newStatus);
		if (newStatus == PaymentStatus.PAID) {
			schedule.setPaidDate(LocalDate.parse(paidDateStr));
			schedule.setPaidAmount(paidAmount != null ? paidAmount : schedule.getEmiAmount());
			schedule.setPendingAmount(BigDecimal.ZERO);
		} else {
			schedule.setPaidDate(null);
			schedule.setPaidAmount(BigDecimal.ZERO);
			schedule.setPendingAmount(schedule.getEmiAmount());
		}

		emiScheduleRepository.save(schedule);

		// --- 2️⃣ Recalculate cumulative pending for the entire customer ---
		calculateCumulativePending(schedule.getCustomerPhone());

		// --- 3️⃣ Update ALL EMI statuses for this customer based on 3-month rule ---
		LocalDate today = LocalDate.now();
		List<EMISchedule> customerSchedules = emiScheduleRepository
				.findByCustomerPhoneOrderByMonth(schedule.getCustomerPhone());

		for (EMISchedule emi : customerSchedules) {
			if (emi.getStatus() != PaymentStatus.PAID) { // Only update non-paid EMIs
				PaymentStatus recalculatedStatus = calculateEMIStatus(emi.getDueDate(), emi.getStatus(), today);
				if (emi.getStatus() != recalculatedStatus) {
					log.info("📅 Status change for {} Month {}: {} -> {}", emi.getCustomerPhone(), emi.getMonthName(),
							emi.getStatus(), recalculatedStatus);
					emi.setStatus(recalculatedStatus);
					emiScheduleRepository.save(emi);
				}
			}
		}

		log.info("✅ Payment status updated and other EMI statuses recalculated for customer {}",
				schedule.getCustomerPhone());
	}

	/**
	 * Get EMI schedule with cumulative pending amounts
	 */
	public List<EMIScheduleResponse> getEMIScheduleWithCumulative(String customerPhone) {
		log.info("🔥 Fetching EMI schedule with cumulative pending for customer: {}", customerPhone);

		// First ensure cumulative amounts are up to date
		calculateCumulativePending(customerPhone);

		List<EMISchedule> schedules = emiScheduleRepository.findByCustomerPhoneOrderByMonth(customerPhone);

		return schedules.stream().map(this::convertToResponse).collect(Collectors.toList());
	}

	/**
	 * 🔥 SMART STATUS UPDATE: Auto-mark overdue EMIs based on 3-month rule
	 */
	@Scheduled(fixedRate = 3600000) // Run every hour
	public void updateOverdueEMIs() {
		log.info("🔥 Running scheduled overdue EMI update with 3-month rule...");

		LocalDate today = LocalDate.now();
		List<EMISchedule> allUnpaidEMIs = emiScheduleRepository.findByStatusNot(PaymentStatus.PAID);

		int updatedCount = 0;

		for (EMISchedule emi : allUnpaidEMIs) {
			PaymentStatus oldStatus = emi.getStatus();
			PaymentStatus newStatus = calculateEMIStatus(emi.getDueDate(), emi.getStatus(), today);

			if (oldStatus != newStatus) {
				emi.setStatus(newStatus);
				emiScheduleRepository.save(emi);
				updatedCount++;

				log.info("📅 EMI Status Updated: Customer={}, Month={}, DueDate={}, {} -> {}", emi.getCustomerPhone(),
						emi.getMonthName(), emi.getDueDate(), oldStatus, newStatus);
			}
		}

		log.info("✅ Overdue EMI update completed. Updated {} EMIs", updatedCount);
	}

	// FIXED: Helper methods with 3-month rule

	/**
	 * FIXED: Update status based on 3-month rule instead of immediate overdue
	 */
	private void updateStatusBasedOnThreeMonthRule(EMISchedule schedule) {
		if (schedule.getStatus() == PaymentStatus.PAID) {
			return; // Don't change paid status
		}

		LocalDate today = LocalDate.now();
		PaymentStatus calculatedStatus = calculateEMIStatus(schedule.getDueDate(), schedule.getStatus(), today);
		schedule.setStatus(calculatedStatus);
	}

	/**
	 * CORE LOGIC: Calculate EMI status based on 3-month rule
	 */
	public static PaymentStatus calculateEMIStatus(LocalDate dueDate, PaymentStatus currentStatus, LocalDate today) {
		if (currentStatus == PaymentStatus.PAID) {
			return PaymentStatus.PAID; // Keep paid status
		}

		if (dueDate.isAfter(today)) {
			return PaymentStatus.PENDING; // Future EMI
		}

		// Calculate months difference including current month
		long monthsDifference = ChronoUnit.MONTHS.between(dueDate.withDayOfMonth(1), today.withDayOfMonth(1)) + 1;

		if (monthsDifference >= 3) {
			return PaymentStatus.OVERDUE; // 3+ months overdue
		} else {
			return PaymentStatus.PENDING; // 1-2 months due
		}
	}

	private String getMonthName(LocalDate date) {
		return date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + date.getYear();
	}

	private EMIScheduleResponse convertToResponse(EMISchedule schedule) {
		EMIScheduleResponse response = new EMIScheduleResponse();
		response.setId(schedule.getId());
		response.setCustomerPhone(schedule.getCustomerPhone());
		response.setMonthNumber(schedule.getMonthNumber());
		response.setMonthName(schedule.getMonthName());
		response.setEmiAmount(schedule.getEmiAmount());
		response.setDueDate(schedule.getDueDate().toString());
		response.setStatus(schedule.getStatus().name());
		response.setPaidDate(schedule.getPaidDate() != null ? schedule.getPaidDate().toString() : null);
		response.setPaidAmount(schedule.getPaidAmount());
		response.setPendingAmount(schedule.getPendingAmount());
		response.setCumulativePending(schedule.getCumulativePending());
		return response;
	}
}
