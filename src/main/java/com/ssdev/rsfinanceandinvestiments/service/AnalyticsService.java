package com.ssdev.rsfinanceandinvestiments.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ssdev.rsfinanceandinvestiments.Enums.MonthCategory;
import com.ssdev.rsfinanceandinvestiments.dto.MonthlyAnalyticsResponse;
import com.ssdev.rsfinanceandinvestiments.dto.RecentPayerResponse;
import com.ssdev.rsfinanceandinvestiments.entity.EMISchedule;
import com.ssdev.rsfinanceandinvestiments.entity.PaymentStatus;
import com.ssdev.rsfinanceandinvestiments.repository.CustomerRepository;
import com.ssdev.rsfinanceandinvestiments.repository.EMIScheduleRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ssdev.rsfinanceandinvestiments.entity.Customer;

@Service
public class AnalyticsService {

	@Autowired
	private EMIScheduleRepository emiScheduleRepository;

	@Autowired
	private CustomerRepository customerRepository;

	private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

public MonthlyAnalyticsResponse getMonthlyAnalytics(MonthCategory monthCategory, int year) {
    List<EMISchedule> filteredSchedules;

    Integer monthNumber = switch (monthCategory) {
        case JANUARY -> 1; case FEBRUARY -> 2; case MARCH -> 3; 
        case APRIL -> 4; case MAY -> 5; case JUNE -> 6;
        case JULY -> 7; case AUGUST -> 8; case SEPTEMBER -> 9;
        case OCTOBER -> 10; case NOVEMBER -> 11; case DECEMBER -> 12;
        default -> null;
    };

    log.info("🚀 ANALYTICS START: Month={}, Year={}, MonthNumber={}", monthCategory, year, monthNumber);

    if (monthNumber != null) {
        filteredSchedules = emiScheduleRepository.findByMonthAndYear(monthNumber, year);
    } else {
        switch (monthCategory) {
            case LAST_3_MONTHS -> filteredSchedules = emiScheduleRepository.findInLastNMonths(LocalDate.now().minusMonths(3));
            case LAST_6_MONTHS -> filteredSchedules = emiScheduleRepository.findInLastNMonths(LocalDate.now().minusMonths(6));
            case ALL -> filteredSchedules = emiScheduleRepository.findAll();
            default -> throw new IllegalArgumentException("Unsupported MonthCategory: " + monthCategory);
        }
    }

    LocalDate today = LocalDate.now();

    Set<String> customerPhones = filteredSchedules.stream()
        .map(EMISchedule::getCustomerPhone)
        .collect(Collectors.toSet());

    int totalCustomers = customerPhones.size();

    int paidCustomers = (int) customerPhones.stream()
        .filter(phone -> filteredSchedules.stream()
            .anyMatch(s -> s.getCustomerPhone().equals(phone) && s.getStatus() == PaymentStatus.PAID))
        .count();

    List<EMISchedule> allSchedules = emiScheduleRepository.findAll();
    List<String> waitlistPhones = emiScheduleRepository.findWaitlistCustomers(today);

    // Overdue = strictly before today
    Set<String> allOverdueCustomers = allSchedules.stream()
        .filter(s -> s.getStatus() != PaymentStatus.PAID && s.getDueDate().isBefore(today))
        .map(EMISchedule::getCustomerPhone)
        .filter(customerPhones::contains)
        .collect(Collectors.toSet());

    Set<String> waitlistCustomersInPeriod = allOverdueCustomers.stream()
        .filter(waitlistPhones::contains)
        .collect(Collectors.toSet());

    // ✅ Pending customers now include overdue + current-month pending
    Set<String> pendingCustomersInPeriod = customerPhones.stream()
        .filter(phone ->
            allSchedules.stream().anyMatch(s -> s.getCustomerPhone().equals(phone)
                    && s.getStatus() != PaymentStatus.PAID
                    && (s.getDueDate().isBefore(today) || s.getDueDate().isEqual(today)))
            && !waitlistCustomersInPeriod.contains(phone)
        ).collect(Collectors.toSet());

    int pendingCustomers = pendingCustomersInPeriod.size();
    int waitlistCustomersCount = waitlistCustomersInPeriod.size();

    BigDecimal totalPaidAmount = filteredSchedules.stream()
        .filter(s -> s.getStatus() == PaymentStatus.PAID)
        .map(EMISchedule::getPaidAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // ✅ Include current-month unpaid in totals
    BigDecimal totalUnpaidAmount = allSchedules.stream()
        .filter(s -> s.getStatus() != PaymentStatus.PAID
                && (s.getDueDate().isBefore(today) || s.getDueDate().isEqual(today)))
        .filter(s -> customerPhones.contains(s.getCustomerPhone()))
        .map(EMISchedule::getEmiAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal pendingUnpaidAmount = allSchedules.stream()
        .filter(s -> s.getStatus() != PaymentStatus.PAID
                && (s.getDueDate().isBefore(today) || s.getDueDate().isEqual(today)))
        .filter(s -> pendingCustomersInPeriod.contains(s.getCustomerPhone()))
        .map(EMISchedule::getEmiAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal waitlistUnpaidAmount = allSchedules.stream()
        .filter(s -> s.getStatus() != PaymentStatus.PAID
                && (s.getDueDate().isBefore(today) || s.getDueDate().isEqual(today)))
        .filter(s -> waitlistCustomersInPeriod.contains(s.getCustomerPhone()))
        .map(EMISchedule::getEmiAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalCollected = totalPaidAmount;
    BigDecimal totalExpected = filteredSchedules.stream()
        .map(EMISchedule::getEmiAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // --- Percentage and direction logic stays exactly the same ---
    LocalDate prevMonthDate = LocalDate.now().minusMonths(1);
    if (monthNumber != null) {
        try { prevMonthDate = LocalDate.of(year, monthNumber, 1).minusMonths(1); }
        catch (Exception e) { log.warn("⚠️ Failed prev month date: {}", e.getMessage()); }
    }
    List<EMISchedule> prevSchedules = emiScheduleRepository.findByMonthAndYear(prevMonthDate.getMonthValue(), prevMonthDate.getYear());
    int prevCustomerCount = (int) prevSchedules.stream().map(EMISchedule::getCustomerPhone).distinct().count();

    double customerGrowthPercentage = prevCustomerCount == 0 ? 100.0 :
        ((totalCustomers - prevCustomerCount) * 100.0) / prevCustomerCount;
    double paidPercentage = totalCustomers == 0 ? 0.0 : (paidCustomers * 100.0) / totalCustomers;
    double pendingPercentage = totalCustomers == 0 ? 0.0 : (pendingCustomers * 100.0) / totalCustomers;
    double waitlistPercentage = totalCustomers == 0 ? 0.0 : (waitlistCustomersCount * 100.0) / totalCustomers;
    double collectionPercentage = totalExpected.compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
        totalCollected.multiply(BigDecimal.valueOf(100)).divide(totalExpected, 2, BigDecimal.ROUND_HALF_UP).doubleValue();

    String customerDirection = customerGrowthPercentage >= 0 ? "+" : "-";
    String paidDirection = paidPercentage >= 50 ? "+" : "-";
    String pendingDirection = pendingPercentage < 50 ? "+" : "-";
    String waitlistDirection = waitlistPercentage < 20 ? "+" : "-";
    String collectionDirection = collectionPercentage >= 80 ? "+" : "-";

    MonthlyAnalyticsResponse response = new MonthlyAnalyticsResponse();
    response.setMonth(monthCategory.name());
    response.setYear(year);
    response.setTotalCustomers(totalCustomers);
    response.setPaidCustomers(paidCustomers);
    response.setPendingCustomers(pendingCustomers);
    response.setWaitlistCustomers(waitlistCustomersCount);
    response.setTotalCollectedAmount(totalCollected);
    response.setTotalPaidAmount(totalPaidAmount);
    response.setTotalUnpaidAmount(totalUnpaidAmount);
    response.setCustomerGrowthPercentage(customerGrowthPercentage);
    response.setPaidPercentage(paidPercentage);
    response.setPendingPercentage(pendingPercentage);
    response.setWaitlistPercentage(waitlistPercentage);
    response.setCollectionPercentage(collectionPercentage);
    response.setCustomerGrowthDirection(customerDirection);
    response.setPaidDirection(paidDirection);
    response.setPendingDirection(pendingDirection);
    response.setWaitlistDirection(waitlistDirection);
    response.setCollectionDirection(collectionDirection);

    return response;
}


	/**
	 * Centralized method to filter EMI schedules by MonthCategory
	 */
	private List<EMISchedule> getFilteredSchedules(MonthCategory monthCategory, int year) {
		List<EMISchedule> filteredSchedules;
		Integer monthNumber = switch (monthCategory) {
		case JANUARY -> 1;
		case FEBRUARY -> 2;
		case MARCH -> 3;
		case APRIL -> 4;
		case MAY -> 5;
		case JUNE -> 6;
		case JULY -> 7;
		case AUGUST -> 8;
		case SEPTEMBER -> 9;
		case OCTOBER -> 10;
		case NOVEMBER -> 11;
		case DECEMBER -> 12;
		default -> null; // for ALL, LAST_3_MONTHS, LAST_6_MONTHS
		};

		log.info("Filtering schedules for {} year={} resolvedMonth={}", monthCategory, year, monthNumber);

		if (monthNumber != null) {
			// Regular month
			filteredSchedules = emiScheduleRepository.findByMonthAndYear(monthNumber, year);
		} else {
			// Range categories
			switch (monthCategory) {
			case LAST_3_MONTHS ->
				filteredSchedules = emiScheduleRepository.findInLastNMonths(LocalDate.now().minusMonths(3));
			case LAST_6_MONTHS ->
				filteredSchedules = emiScheduleRepository.findInLastNMonths(LocalDate.now().minusMonths(6));
			case ALL -> filteredSchedules = emiScheduleRepository.findAll(); // If supported
			default -> throw new IllegalArgumentException("Unhandled MonthCategory: " + monthCategory);
			}
		}

		log.debug("Total EMI schedules found: {}", filteredSchedules.size());
		return filteredSchedules;
	}

	/**
	 * Build RecentPayerResponse from phone
	 */
	// Helper: Build RecentPayerResponse for given phone with all schedules, sets
	// balance
	private RecentPayerResponse buildResponseWithBalance(String phone, List<EMISchedule> allSchedules) {
		RecentPayerResponse dto = new RecentPayerResponse();
		Customer customer = customerRepository.findByPhoneNumber(phone).orElse(null);

		// All EMI schedules for this customer (entire tenure)
		List<EMISchedule> customerSchedules = allSchedules.stream().filter(s -> s.getCustomerPhone().equals(phone))
				.toList();

		dto.setCustomer(customer);
		dto.setEmiSchedules(customerSchedules);

		// Calculate balance = sum of all unpaid EMI pendingAmount
		BigDecimal balance = customerSchedules.stream()
				.filter(s -> s.getStatus() == PaymentStatus.PENDING || s.getStatus() == PaymentStatus.OVERDUE)
				.map(EMISchedule::getPendingAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

		dto.setBalance(balance);
		return dto;
	}


// 2️⃣ Paid customers
public List<RecentPayerResponse> getPaidCustomers(MonthCategory monthCategory, int year) {
    long startTime = System.currentTimeMillis();
    log.info("Fetching PAID customers for {} year={}", monthCategory, year);

    LocalDate today = LocalDate.now();
    List<EMISchedule> filteredSchedules = getFilteredSchedules(monthCategory, year);
    List<EMISchedule> allSchedules = emiScheduleRepository.findAll();

    List<RecentPayerResponse> response = filteredSchedules.stream()
            .filter(s -> s.getStatus() == PaymentStatus.PAID)
            .map(EMISchedule::getCustomerPhone)
            .distinct()
            .map(phone -> {
                RecentPayerResponse dto = new RecentPayerResponse();
                Customer customer = customerRepository.findByPhoneNumber(phone).orElse(null);
                dto.setCustomer(customer);

                dto.setEmiSchedules(filteredSchedules.stream()
                        .filter(s -> s.getCustomerPhone().equals(phone) && s.getStatus() == PaymentStatus.PAID)
                        .toList());

                // ✅ Include EMIs due on/before today in balance
                BigDecimal balance = allSchedules.stream()
                        .filter(s -> s.getCustomerPhone().equals(phone))
                        .filter(s -> s.getStatus() == PaymentStatus.PENDING || s.getStatus() == PaymentStatus.OVERDUE)
                        .map(EMISchedule::getEmiAmount)  // Sum full EMI amount
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                dto.setBalance(balance);
                log.info("📊 PAID Customer={} Balance={}", phone, balance);
                return dto;
            })
            .toList();

    log.info("Total paid customers: {}", response.size());
    log.debug("Execution time: {} ms", (System.currentTimeMillis() - startTime));
    return response;
}

// 3️⃣ Pending customers - EMI schedules up to current month, balance covers full tenure
public List<RecentPayerResponse> getPendingCustomers(MonthCategory monthCategory, int year) {
    long startTime = System.currentTimeMillis();
    log.info("🚀 FETCHING PENDING CUSTOMERS: Month={}, Year={}", monthCategory, year);

    List<EMISchedule> filteredSchedules = getFilteredSchedules(monthCategory, year);
    List<EMISchedule> allSchedules = emiScheduleRepository.findAll();
    LocalDate today = LocalDate.now();

    List<String> waitlistPhones = emiScheduleRepository.findWaitlistCustomers(today);

    Set<String> filteredCustomerPhones = filteredSchedules.stream()
            .map(EMISchedule::getCustomerPhone)
            .collect(Collectors.toSet());

    List<RecentPayerResponse> response = filteredCustomerPhones.stream()
            .filter(phone -> {
                boolean hasOverdueOrDueToday = allSchedules.stream()
                        .anyMatch(s -> s.getCustomerPhone().equals(phone)
                                && s.getStatus() != PaymentStatus.PAID
                                && (s.getDueDate().isBefore(today) || s.getDueDate().isEqual(today)));
                boolean isWaitlist = waitlistPhones.contains(phone);
                return hasOverdueOrDueToday && !isWaitlist;
            })
            .map(phone -> {
                List<EMISchedule> upToCurrentMonthSchedules = allSchedules.stream()
                        .filter(s -> s.getCustomerPhone().equals(phone))
                        .filter(s -> s.getDueDate().isBefore(today)
                                || s.getDueDate().isEqual(today)
                                || (s.getDueDate().getYear() == today.getYear()
                                    && s.getDueDate().getMonthValue() == today.getMonthValue()))
                        .sorted((s1, s2) -> s1.getMonthNumber().compareTo(s2.getMonthNumber()))
                        .toList();

                RecentPayerResponse dto = buildResponseWithBalance(phone, allSchedules);
                dto.setEmiSchedules(upToCurrentMonthSchedules);
                return dto;
            })
            .toList();

    log.info("🎯 FINAL: Total pending customers: {}", response.size());
    log.debug("Execution time: {} ms", (System.currentTimeMillis() - startTime));
    return response;
}

// 1️⃣ All customers with balance = sum of all unpaid EMI amounts due <= today
public List<RecentPayerResponse> getAllCustomers(MonthCategory monthCategory, int year) {
    long startTime = System.currentTimeMillis();
    log.info("Fetching ALL customers for {} year={}", monthCategory, year);

    LocalDate today = LocalDate.now();

    // EMIs for the selected month/period
    List<EMISchedule> filteredSchedules = getFilteredSchedules(monthCategory, year);

    // All schedules for balance calculation (full tenure data)
    List<EMISchedule> allSchedules = emiScheduleRepository.findAll();

    List<RecentPayerResponse> response = filteredSchedules.stream()
            .map(EMISchedule::getCustomerPhone)
            .distinct()
            .map(phone -> {
                RecentPayerResponse dto = new RecentPayerResponse();

                // Customer info
                Customer customer = customerRepository.findByPhoneNumber(phone).orElse(null);
                dto.setCustomer(customer);

                // 🔹 EMI schedules for the selected month/period only
                dto.setEmiSchedules(filteredSchedules.stream()
                        .filter(s -> s.getCustomerPhone().equals(phone))
                        .toList());

                BigDecimal balance = allSchedules.stream()
                        .filter(s -> s.getCustomerPhone().equals(phone))
                        .filter(s -> s.getStatus() == PaymentStatus.PENDING || s.getStatus() == PaymentStatus.OVERDUE)
                        .map(EMISchedule::getEmiAmount)  // Sum full EMI amount
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                dto.setBalance(balance);

                return dto;
            })
            .toList();

    log.info("Total customers found: {}", response.size());
    log.debug("Execution time: {} ms", (System.currentTimeMillis() - startTime));
    return response;
}




//4️⃣ Waitlist customers (only EMIs due <= today)
public List<RecentPayerResponse> getWaitlistCustomers() {
 long startTime = System.currentTimeMillis();
 LocalDate today = LocalDate.now();
 LocalDate cutoff = today.minusMonths(3);

 log.info("Fetching WAITLIST customers pending before {}", cutoff);

 List<EMISchedule> allSchedules = emiScheduleRepository.findAll();
 List<String> waitlistPhones = emiScheduleRepository.findWaitlistCustomers(cutoff);

 List<RecentPayerResponse> response = waitlistPhones.stream()
         .map(phone -> {
             RecentPayerResponse dto = new RecentPayerResponse();
             Customer customer = customerRepository.findByPhoneNumber(phone).orElse(null);
             dto.setCustomer(customer);

             dto.setEmiSchedules(
                     emiScheduleRepository.findByCustomerPhoneOrderByMonth(phone)
             );

             BigDecimal balance = allSchedules.stream()
            	        .filter(s -> s.getCustomerPhone().equals(phone))
            	        .filter(s -> s.getStatus() == PaymentStatus.PENDING || s.getStatus() == PaymentStatus.OVERDUE)
            	        .map(EMISchedule::getEmiAmount)  // Sum full EMI amount
            	        .reduce(BigDecimal.ZERO, BigDecimal::add);

             dto.setBalance(balance);
             return dto;
         })
         .toList();

 log.info("Total waitlist customers: {}", response.size());
 log.debug("Execution time: {} ms", (System.currentTimeMillis() - startTime));
 return response;
}






}