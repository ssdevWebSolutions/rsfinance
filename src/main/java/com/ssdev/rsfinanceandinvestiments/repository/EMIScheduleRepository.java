package com.ssdev.rsfinanceandinvestiments.repository;

import org.springframework.data.domain.Pageable; // ✅ CORRECT

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ssdev.rsfinanceandinvestiments.entity.EMISchedule;

//EMIScheduleRepository.java
@Repository
public interface EMIScheduleRepository extends JpaRepository<EMISchedule, Long> {
 
 @Query("SELECT e FROM EMISchedule e WHERE e.customerPhone = :customerPhone ORDER BY e.monthNumber ASC")
 List<EMISchedule> findByCustomerPhoneOrderByMonth(@Param("customerPhone") String customerPhone);
 
 @Query("SELECT e FROM EMISchedule e WHERE e.customerPhone = :customerPhone AND e.monthNumber <= :currentMonth ORDER BY e.monthNumber ASC")
 List<EMISchedule> findPendingEMIsUpToMonth(@Param("customerPhone") String customerPhone, @Param("currentMonth") Integer currentMonth);
 
 @Query("SELECT e FROM EMISchedule e WHERE e.status = 'OVERDUE' AND e.dueDate < CURRENT_DATE")
 List<EMISchedule> findOverdueEMIs();
 
 @Query("SELECT SUM(e.pendingAmount) FROM EMISchedule e WHERE e.customerPhone = :customerPhone AND e.status != 'PAID'")
 BigDecimal getTotalPendingAmount(@Param("customerPhone") String customerPhone);
 
 
 @Query("SELECT e FROM EMISchedule e WHERE e.status = 'PAID' AND e.paidDate IS NOT NULL ORDER BY e.paidDate DESC")
 List<EMISchedule> findTop20PaidWithCustomer(Pageable pageable);
 
 @Query("SELECT e FROM EMISchedule e WHERE EXTRACT(MONTH FROM e.dueDate) = :month AND EXTRACT(YEAR FROM e.dueDate) = :year")
 List<EMISchedule> findByMonthAndYear(@Param("month") int month, @Param("year") int year);




 @Query("SELECT e FROM EMISchedule e WHERE e.dueDate >= :startDate")
 List<EMISchedule> findInLastNMonths(@Param("startDate") LocalDate startDate);

 @Query("SELECT e.customerPhone FROM EMISchedule e " +
	       "WHERE e.status != 'PAID' AND e.dueDate <= :olderThanDate " +
	       "GROUP BY e.customerPhone " +
	       "HAVING COUNT(e) >= 3")
	List<String> findWaitlistCustomers(@Param("olderThanDate") LocalDate olderThanDate);


}
