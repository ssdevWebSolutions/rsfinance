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
import com.ssdev.rsfinanceandinvestiments.entity.EMISchedule;
import com.ssdev.rsfinanceandinvestiments.entity.PaymentStatus;
import com.ssdev.rsfinanceandinvestiments.repository.CustomerRepository;
import com.ssdev.rsfinanceandinvestiments.repository.EMIScheduleRepository;

@Service
public class AnalyticsService {

    @Autowired
    private EMIScheduleRepository emiScheduleRepository;

    @Autowired
    private CustomerRepository customerRepository;

    public MonthlyAnalyticsResponse getMonthlyAnalytics(MonthCategory monthCategory, int year) {
        List<EMISchedule> filteredSchedules;

        int monthNumber = monthCategory.ordinal() - 2;
        System.out.println(monthCategory + " " + year + " " + monthNumber + "-> ");

        switch (monthCategory) {
            case LAST_3_MONTHS:
                filteredSchedules = emiScheduleRepository.findInLastNMonths(LocalDate.now().minusMonths(3));
                break;
            case LAST_6_MONTHS:
                filteredSchedules = emiScheduleRepository.findInLastNMonths(LocalDate.now().minusMonths(6));
                break;
            default:
                filteredSchedules = emiScheduleRepository.findByMonthAndYear(monthNumber, year);
                break;
        }

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

        BigDecimal totalCollected = filteredSchedules.stream()
                .filter(s -> s.getStatus() == PaymentStatus.PAID)
                .map(EMISchedule::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpected = filteredSchedules.stream()
                .map(EMISchedule::getEmiAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Previous month data
        LocalDate prevMonthDate = LocalDate.of(year, monthNumber, 1).minusMonths(1);
        List<EMISchedule> prevSchedules = emiScheduleRepository.findByMonthAndYear(
                prevMonthDate.getMonthValue(), prevMonthDate.getYear());

        int prevCustomerCount = (int) prevSchedules.stream()
                .map(EMISchedule::getCustomerPhone)
                .distinct()
                .count();

        // === Percentage Calculations ===
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

        // === Direction Logic (based on performance) ===
        String customerDirection = customerGrowthPercentage >= 0 ? "+" : "-";
        String paidDirection = paidPercentage >= 50 ? "+" : "-";
        String pendingDirection = pendingPercentage < 50 ? "+" : "-";
        String waitlistDirection = waitlistPercentage < 20 ? "+" : "-";
        String collectionDirection = collectionPercentage >= 80 ? "+" : "-";

        // === Build Response ===
        MonthlyAnalyticsResponse response = new MonthlyAnalyticsResponse();
        response.setMonth(monthCategory.name());
        response.setYear(year);
        response.setTotalCustomers(totalCustomers);
        response.setPaidCustomers(paidCustomers);
        response.setPendingCustomers(pendingCustomers);
        response.setWaitlistCustomers(waitlistPhones.size());
        response.setTotalCollectedAmount(totalCollected);

        // Percentages
        response.setCustomerGrowthPercentage(customerGrowthPercentage);
        response.setPaidPercentage(paidPercentage);
        response.setPendingPercentage(pendingPercentage);
        response.setWaitlistPercentage(waitlistPercentage);
        response.setCollectionPercentage(collectionPercentage);

        // Directions
        response.setCustomerGrowthDirection(customerDirection);
        response.setPaidDirection(paidDirection);
        response.setPendingDirection(pendingDirection);
        response.setWaitlistDirection(waitlistDirection);
        response.setCollectionDirection(collectionDirection);

        return response;
    }
}
