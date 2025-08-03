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


//    Dashborad api - To get all responses 
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

    // 2. Get filtered data
    if (monthNumber != null) {
        // Use specific month and year
        filteredSchedules = emiScheduleRepository.findByMonthAndYear(monthNumber, year);
    } else {
        switch (monthCategory) {
            case LAST_3_MONTHS -> filteredSchedules = emiScheduleRepository.findInLastNMonths(LocalDate.now().minusMonths(3));
            case LAST_6_MONTHS -> filteredSchedules = emiScheduleRepository.findInLastNMonths(LocalDate.now().minusMonths(6));
            case ALL -> filteredSchedules = emiScheduleRepository.findAll(); // or custom logic
            default -> throw new IllegalArgumentException("Unsupported MonthCategory: " + monthCategory);
        }
    }

    // 3. Extract customer phones
    Set<String> customerPhones = filteredSchedules.stream()
            .map(EMISchedule::getCustomerPhone)
            .collect(Collectors.toSet());

    int totalCustomers = customerPhones.size();

    int paidCustomers = (int) customerPhones.stream()
            .filter(phone -> filteredSchedules.stream()
                    .anyMatch(s -> s.getCustomerPhone().equals(phone) && s.getStatus() == PaymentStatus.PAID))
            .count();

    int pendingCustomers = totalCustomers - paidCustomers;

    List<String> waitlistPhones = emiScheduleRepository.findWaitlistCustomers(LocalDate.now().minusMonths(3));
    
    BigDecimal totalPaidAmount = filteredSchedules.stream()
            .filter(s -> s.getStatus() == PaymentStatus.PAID)
            .map(EMISchedule::getPaidAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalUnpaidAmount = filteredSchedules.stream()
            .filter(s -> s.getStatus() != PaymentStatus.PAID)
            .map(EMISchedule::getEmiAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);


    BigDecimal totalCollected = filteredSchedules.stream()
            .filter(s -> s.getStatus() == PaymentStatus.PAID)
            .map(EMISchedule::getPaidAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalExpected = filteredSchedules.stream()
            .map(EMISchedule::getEmiAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    // 4. Previous month comparison
    LocalDate prevMonthDate = LocalDate.now().minusMonths(1);  // Default to previous month

    if (monthNumber != null) {
        try {
            prevMonthDate = LocalDate.of(year, monthNumber, 1).minusMonths(1);
        } catch (Exception e) {
            System.out.println("⚠️ Failed to resolve previous month date: " + e.getMessage());
        }
    }

    List<EMISchedule> prevSchedules = emiScheduleRepository.findByMonthAndYear(
            prevMonthDate.getMonthValue(), prevMonthDate.getYear());

    int prevCustomerCount = (int) prevSchedules.stream()
            .map(EMISchedule::getCustomerPhone)
            .distinct()
            .count();

    // === 5. Calculations ===
    double customerGrowthPercentage = prevCustomerCount == 0 ? 100.0 :
            ((totalCustomers - prevCustomerCount) * 100.0) / prevCustomerCount;

    double paidPercentage = totalCustomers == 0 ? 0.0 :
            (paidCustomers * 100.0) / totalCustomers;

    double pendingPercentage = totalCustomers == 0 ? 0.0 :
            (pendingCustomers * 100.0) / totalCustomers;

    double waitlistPercentage = totalCustomers == 0 ? 0.0 :
            (waitlistPhones.size() * 100.0) / totalCustomers;

    double collectionPercentage = totalExpected.compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
            totalCollected.multiply(BigDecimal.valueOf(100))
                    .divide(totalExpected, 2, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();

    // === 6. Direction Logic ===
    String customerDirection = customerGrowthPercentage >= 0 ? "+" : "-";
    String paidDirection = paidPercentage >= 50 ? "+" : "-";
    String pendingDirection = pendingPercentage < 50 ? "+" : "-";
    String waitlistDirection = waitlistPercentage < 20 ? "+" : "-";
    String collectionDirection = collectionPercentage >= 80 ? "+" : "-";

    // === 7. Build Response ===
    MonthlyAnalyticsResponse response = new MonthlyAnalyticsResponse();
    response.setMonth(monthCategory.name());
    response.setYear(year);
    response.setTotalCustomers(totalCustomers);
    response.setPaidCustomers(paidCustomers);
    response.setPendingCustomers(pendingCustomers);
    response.setWaitlistCustomers(waitlistPhones.size());
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
