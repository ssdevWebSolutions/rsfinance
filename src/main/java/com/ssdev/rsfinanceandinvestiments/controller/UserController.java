package com.ssdev.rsfinanceandinvestiments.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/me")
    public String getCurrentUser(Authentication authentication) {
    	System.out.print("hello"+authentication.getName());
        return authentication.getName();
    }
} 
