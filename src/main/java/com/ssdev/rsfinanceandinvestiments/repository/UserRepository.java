package com.ssdev.rsfinanceandinvestiments.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssdev.rsfinanceandinvestiments.entity.AppUser;



public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
}

