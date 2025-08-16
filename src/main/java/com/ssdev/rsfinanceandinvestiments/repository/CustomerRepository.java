package com.ssdev.rsfinanceandinvestiments.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ssdev.rsfinanceandinvestiments.entity.Customer;


@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long> {
	
	Optional<Customer> findByPhoneNumber(String phoneNumber);
	
	void deleteByPhoneNumber(String phoneNumber);

	long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
	
	@Query("SELECT COUNT(DISTINCT e.customerPhone) FROM EMISchedule e WHERE e.dueDate BETWEEN :startDate AND :endDate")
	long countCustomersWithEMIsInDateRange(@Param("startDate") LocalDate startDate, 
	                                       @Param("endDate") LocalDate endDate);




}
