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


// Dashboard api - To get all responses (FIXED with proper waitlist query)
public MonthlyAnalyticsResponse getMonthlyAnalytics(MonthCategory monthCategory, int year) {
    List<EMISchedule> filteredSchedules;

    // 1. Map enum to month number (null means special handling)
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
        default -> null;  // For LAST_3_MONTHS, LAST_6_MONTHS, ALL
    };

    log.info("🚀 ANALYTICS START: Month={}, Year={}, MonthNumber={}", monthCategory, year, monthNumber);

    // 2. Get filtered data
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
    log.info("📅 DEBUG: Today's date: {}", today);
    log.info("📊 DEBUG: Filtered schedules count: {}", filteredSchedules.size());

    // Debug: Show some filtered schedule details
    filteredSchedules.stream().limit(5).forEach(schedule -> 
        log.info("  📋 Filtered Schedule: Phone={}, Month={}, DueDate={}, Status={}", 
            schedule.getCustomerPhone(), schedule.getMonthName(), schedule.getDueDate(), schedule.getStatus())
    );

    // 3. Extract customer phones from filtered schedules
    Set<String> customerPhones = filteredSchedules.stream()
            .map(EMISchedule::getCustomerPhone)
            .collect(Collectors.toSet());

    int totalCustomers = customerPhones.size();
    log.info("👥 DEBUG: Total customers in filtered period: {}", totalCustomers);
    log.info("📞 DEBUG: Customer phones: {}", customerPhones);

    // 4. Count paid customers
    int paidCustomers = (int) customerPhones.stream()
            .filter(phone -> filteredSchedules.stream()
                    .anyMatch(s -> s.getCustomerPhone().equals(phone) && s.getStatus() == PaymentStatus.PAID))
            .count();

    log.info("💰 DEBUG: Paid customers count: {}", paidCustomers);

    // Debug: Show which customers are paid
    customerPhones.stream().forEach(phone -> {
        boolean hasPaid = filteredSchedules.stream()
                .anyMatch(s -> s.getCustomerPhone().equals(phone) && s.getStatus() == PaymentStatus.PAID);
        log.info("  💳 Customer {} has paid EMI in filtered period: {}", phone, hasPaid);
    });

    // 5. Get ALL schedules to properly calculate overdue EMIs
    List<EMISchedule> allSchedules = emiScheduleRepository.findAll();
    log.info("📊 DEBUG: Total ALL schedules count: {}", allSchedules.size());

    // 6. FIXED: Get waitlist customers using today's date to count ALL overdue EMIs
    List<String> waitlistPhones = emiScheduleRepository.findWaitlistCustomers(today);
    log.info("⚠️ DEBUG: Waitlist phones from query (3+ overdue): {}", waitlistPhones);

    // 7. Separate customers based on overdue EMIs
    Set<String> allOverdueCustomers = allSchedules.stream()
            .filter(s -> s.getStatus() != PaymentStatus.PAID && s.getDueDate().isBefore(today))
            .map(EMISchedule::getCustomerPhone)
            .filter(customerPhones::contains) // Only customers from filtered period
            .collect(Collectors.toSet());

    log.info("🔥 DEBUG: All overdue customers from filtered period: {}", allOverdueCustomers);

    // Debug: Show overdue EMI count for each customer
    for (String phone : customerPhones) {
        List<EMISchedule> customerOverdueEMIs = allSchedules.stream()
                .filter(s -> s.getCustomerPhone().equals(phone) 
                        && s.getStatus() != PaymentStatus.PAID 
                        && s.getDueDate().isBefore(today))
                .toList();
        
        log.info("📱 DEBUG: Customer {} has {} overdue EMIs:", phone, customerOverdueEMIs.size());
        customerOverdueEMIs.forEach(emi -> 
            log.info("    📅 Overdue EMI: Month={}, DueDate={}, Amount={}", 
                emi.getMonthName(), emi.getDueDate(), emi.getEmiAmount())
        );
    }

    // Waitlist customers (from filtered customers who are in waitlist)
    Set<String> waitlistCustomersInPeriod = allOverdueCustomers.stream()
            .filter(waitlistPhones::contains)
            .collect(Collectors.toSet());

    // Pending customers (overdue but not in waitlist - means 1-2 overdue EMIs)
    Set<String> pendingCustomersInPeriod = allOverdueCustomers.stream()
            .filter(phone -> !waitlistPhones.contains(phone))
            .collect(Collectors.toSet());

    int pendingCustomers = pendingCustomersInPeriod.size();
    int waitlistCustomersCount = waitlistCustomersInPeriod.size();

    log.info("⏰ DEBUG: Pending customers (1-2 overdue EMIs): {} - {}", pendingCustomers, pendingCustomersInPeriod);
    log.info("⚠️ DEBUG: Waitlist customers (3+ overdue EMIs): {} - {}", waitlistCustomersCount, waitlistCustomersInPeriod);

    // 8. Amount calculations
    BigDecimal totalPaidAmount = filteredSchedules.stream()
            .filter(s -> s.getStatus() == PaymentStatus.PAID)
            .map(EMISchedule::getPaidAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    log.info("💵 DEBUG: Total paid amount from filtered schedules: {}", totalPaidAmount);

    // Only overdue unpaid amount (from previous months)
    BigDecimal totalUnpaidAmount = allSchedules.stream()
            .filter(s -> s.getStatus() != PaymentStatus.PAID && s.getDueDate().isBefore(today))
            .filter(s -> customerPhones.contains(s.getCustomerPhone()))
            .map(EMISchedule::getEmiAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    log.info("🔴 DEBUG: Total unpaid amount (overdue only): {}", totalUnpaidAmount);

    // Debug: Break down unpaid amount by customer type
    BigDecimal pendingUnpaidAmount = allSchedules.stream()
            .filter(s -> s.getStatus() != PaymentStatus.PAID && s.getDueDate().isBefore(today))
            .filter(s -> pendingCustomersInPeriod.contains(s.getCustomerPhone()))
            .map(EMISchedule::getEmiAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal waitlistUnpaidAmount = allSchedules.stream()
            .filter(s -> s.getStatus() != PaymentStatus.PAID && s.getDueDate().isBefore(today))
            .filter(s -> waitlistCustomersInPeriod.contains(s.getCustomerPhone()))
            .map(EMISchedule::getEmiAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    log.info("💰 DEBUG: Pending customers unpaid amount: {}", pendingUnpaidAmount);
    log.info("⚠️ DEBUG: Waitlist customers unpaid amount: {}", waitlistUnpaidAmount);

    BigDecimal totalCollected = totalPaidAmount;

    BigDecimal totalExpected = filteredSchedules.stream()
            .map(EMISchedule::getEmiAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    log.info("🎯 DEBUG: Total expected amount: {}", totalExpected);
    log.info("✅ DEBUG: Total collected amount: {}", totalCollected);

    // 9. Previous month comparison
    LocalDate prevMonthDate = LocalDate.now().minusMonths(1);

    if (monthNumber != null) {
        try {
            prevMonthDate = LocalDate.of(year, monthNumber, 1).minusMonths(1);
        } catch (Exception e) {
            System.out.println("⚠️ Failed to resolve previous month date: " + e.getMessage());
        }
    }

    log.info("📅 DEBUG: Previous month date for comparison: {}", prevMonthDate);

    List<EMISchedule> prevSchedules = emiScheduleRepository.findByMonthAndYear(
            prevMonthDate.getMonthValue(), prevMonthDate.getYear());

    int prevCustomerCount = (int) prevSchedules.stream()
            .map(EMISchedule::getCustomerPhone)
            .distinct()
            .count();

    log.info("👥 DEBUG: Previous month customer count: {}", prevCustomerCount);

    // === 10. Calculations ===
    double customerGrowthPercentage = prevCustomerCount == 0 ? 100.0 :
            ((totalCustomers - prevCustomerCount) * 100.0) / prevCustomerCount;

    double paidPercentage = totalCustomers == 0 ? 0.0 :
            (paidCustomers * 100.0) / totalCustomers;

    double pendingPercentage = totalCustomers == 0 ? 0.0 :
            (pendingCustomers * 100.0) / totalCustomers;

    double waitlistPercentage = totalCustomers == 0 ? 0.0 :
            (waitlistCustomersCount * 100.0) / totalCustomers;

    double collectionPercentage = totalExpected.compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
            totalCollected.multiply(BigDecimal.valueOf(100))
                    .divide(totalExpected, 2, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();

    log.info("📊 DEBUG: Customer growth: {}%", customerGrowthPercentage);
    log.info("💰 DEBUG: Paid percentage: {}%", paidPercentage);
    log.info("⏰ DEBUG: Pending percentage: {}%", pendingPercentage);
    log.info("⚠️ DEBUG: Waitlist percentage: {}%", waitlistPercentage);
    log.info("💵 DEBUG: Collection percentage: {}%", collectionPercentage);

    // === 11. Direction Logic ===
    String customerDirection = customerGrowthPercentage >= 0 ? "+" : "-";
    String paidDirection = paidPercentage >= 50 ? "+" : "-";
    String pendingDirection = pendingPercentage < 50 ? "+" : "-";
    String waitlistDirection = waitlistPercentage < 20 ? "+" : "-";
    String collectionDirection = collectionPercentage >= 80 ? "+" : "-";

    log.info("🎯 DEBUG: Directions - Customer: {}, Paid: {}, Pending: {}, Waitlist: {}, Collection: {}", 
            customerDirection, paidDirection, pendingDirection, waitlistDirection, collectionDirection);

    // === 12. Build Response ===
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

    // Set percentages
    response.setCustomerGrowthPercentage(customerGrowthPercentage);
    response.setPaidPercentage(paidPercentage);
    response.setPendingPercentage(pendingPercentage);
    response.setWaitlistPercentage(waitlistPercentage);
    response.setCollectionPercentage(collectionPercentage);

    // Set directions
    response.setCustomerGrowthDirection(customerDirection);
    response.setPaidDirection(paidDirection);
    response.setPendingDirection(pendingDirection);
    response.setWaitlistDirection(waitlistDirection);
    response.setCollectionDirection(collectionDirection);

    log.info("🎉 FINAL RESPONSE: Total={}, Paid={}, Pending={}, Waitlist={}, PaidAmt={}, UnpaidAmt={}", 
            totalCustomers, paidCustomers, pendingCustomers, waitlistCustomersCount, 
            totalPaidAmount, totalUnpaidAmount);

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
        private RecentPayerResponse buildResponse(String phone, List<EMISchedule> allSchedules) {
            RecentPayerResponse dto = new RecentPayerResponse();
            Customer customer = customerRepository.findByPhoneNumber(phone).orElse(null);

            dto.setCustomer(customer);
            dto.setEmiSchedules(
                    allSchedules.stream()
                            .filter(s -> s.getCustomerPhone().equals(phone))
                            .toList()
            );

            return dto;
        }

        // 1️⃣ All customers
        public List<RecentPayerResponse> getAllCustomers(MonthCategory monthCategory, int year) {
            long startTime = System.currentTimeMillis();
            log.info("Fetching ALL customers for {} year={}", monthCategory, year);

            List<EMISchedule> schedules = getFilteredSchedules(monthCategory, year);
            List<RecentPayerResponse> response = schedules.stream()
                    .map(EMISchedule::getCustomerPhone)
                    .distinct()
                    .map(phone -> buildResponse(phone, schedules))
                    .toList();

            log.info("Total customers found: {}", response.size());
            log.debug("Execution time: {} ms", (System.currentTimeMillis() - startTime));
            return response;
        }

        // 2️⃣ Paid customers
        public List<RecentPayerResponse> getPaidCustomers(MonthCategory monthCategory, int year) {
            long startTime = System.currentTimeMillis();
            log.info("Fetching PAID customers for {} year={}", monthCategory, year);

            List<EMISchedule> schedules = getFilteredSchedules(monthCategory, year);
            List<RecentPayerResponse> response = schedules.stream()
                    .filter(s -> s.getStatus() == PaymentStatus.PAID)
                    .map(EMISchedule::getCustomerPhone)
                    .distinct()
                    .map(phone -> {
                        RecentPayerResponse dto = new RecentPayerResponse();
                        dto.setCustomer(customerRepository.findByPhoneNumber(phone).orElse(null));
                        dto.setEmiSchedules(schedules.stream()
                                .filter(s -> s.getCustomerPhone().equals(phone) && s.getStatus() == PaymentStatus.PAID)
                                .toList());
                        return dto;
                    })
                    .toList();

            log.info("Total paid customers: {}", response.size());
            log.debug("Execution time: {} ms", (System.currentTimeMillis() - startTime));
            return response;
        }

        // 3️⃣ Pending customers
        public List<RecentPayerResponse> getPendingCustomers(MonthCategory monthCategory, int year) {
            long startTime = System.currentTimeMillis();
            log.info("Fetching PENDING customers for {} year={}", monthCategory, year);

            List<EMISchedule> schedules = getFilteredSchedules(monthCategory, year);
            List<RecentPayerResponse> response = schedules.stream()
                    .filter(s -> s.getStatus() != PaymentStatus.PAID)
                    .map(EMISchedule::getCustomerPhone)
                    .distinct()
                    .map(phone -> {
                        RecentPayerResponse dto = new RecentPayerResponse();
                        dto.setCustomer(customerRepository.findByPhoneNumber(phone).orElse(null));
                        dto.setEmiSchedules(schedules.stream()
                                .filter(s -> s.getCustomerPhone().equals(phone) && s.getStatus() != PaymentStatus.PAID)
                                .toList());
                        return dto;
                    })
                    .toList();

            log.info("Total pending customers: {}", response.size());
            log.debug("Execution time: {} ms", (System.currentTimeMillis() - startTime));
            return response;
        }

        // 4️⃣ Waitlist customers (>=3 months pending from today)
        public List<RecentPayerResponse> getWaitlistCustomers() {
            long startTime = System.currentTimeMillis();
            LocalDate cutoff = LocalDate.now().minusMonths(3);
            log.info("Fetching WAITLIST customers pending before {}", cutoff);

            List<String> waitlistPhones = emiScheduleRepository.findWaitlistCustomers(cutoff);
            log.debug("Waitlist phones: {}", waitlistPhones);

            List<RecentPayerResponse> response = waitlistPhones.stream()
                    .map(phone -> {
                        RecentPayerResponse dto = new RecentPayerResponse();
                        dto.setCustomer(customerRepository.findByPhoneNumber(phone).orElse(null));
                        dto.setEmiSchedules(
                                emiScheduleRepository.findByCustomerPhoneOrderByMonth(phone)
                        );
                        return dto;
                    })
                    .toList();

            log.info("Total waitlist customers: {}", response.size());
            log.debug("Execution time: {} ms", (System.currentTimeMillis() - startTime));
            return response;
        }
    

    
    
    
}