package com.ssdev.rsfinanceandinvestiments.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ssdev.rsfinanceandinvestiments.dto.OtpVerificationRequest;
import com.ssdev.rsfinanceandinvestiments.dto.ResetPasswordRequest;
import com.ssdev.rsfinanceandinvestiments.service.UserService;
import com.ssdev.rsfinanceandinvestiments.utility.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
public class UserController {
	
	@Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/me")
    public String getCurrentUser(Authentication authentication) {
    	System.out.print("hello"+authentication.getName());
        return authentication.getName();
    }
    
    
    
    
    @PostMapping("/test-body")
    public ResponseEntity<String> testBodyReceiver(@RequestBody ResetPasswordRequest requestBody) {
        System.out.println("✅ Received test field: " + requestBody.getNewPassword());
        return ResponseEntity.ok("Body received successfully");
    }
    
    @PostMapping("/request-reset-password")
    public ResponseEntity<String> requestResetPassword(
            @RequestBody ResetPasswordRequest passwordRequest,
            HttpServletRequest request) {
        
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Missing or invalid Authorization header");
        }

        token = token.substring(7); // Remove "Bearer "
        String email;
        try {
            email = jwtUtil.extractUsername(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token");
        }

        System.out.println("✅ Email extracted from token: " + email);

        userService.storePendingPassword(email, passwordRequest.getNewPassword());
        userService.generateAndSendOtp(email);

        return ResponseEntity.ok("OTP sent to your email. Password will be updated after verification.");
    }
    
    @PostMapping("/confirm-reset-password")
    public ResponseEntity<String> confirmResetPassword(@RequestBody OtpVerificationRequest otpRequest,
                                                       HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").substring(7);
            String email = jwtUtil.extractUsername(token);
            System.out.println("Verifying OTP for: " + email);
            System.out.println(otpRequest.getOtp()+" "+"otp");

            boolean isValid = userService.verifyOtp(email, otpRequest.getOtp());

            if (!isValid) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP");
            }

            userService.commitPendingPassword(email);
            return ResponseEntity.ok("Password updated successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Failed to reset password: " + e.getMessage());
        }
    }



    
} 
