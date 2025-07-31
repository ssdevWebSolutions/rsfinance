package com.ssdev.rsfinanceandinvestiments.controller;

import com.ssdev.rsfinanceandinvestiments.dto.AuthResponse;
import com.ssdev.rsfinanceandinvestiments.dto.LoginRequest;
import com.ssdev.rsfinanceandinvestiments.dto.OtpVerificationRequest;
import com.ssdev.rsfinanceandinvestiments.dto.RegisterRequest;
import com.ssdev.rsfinanceandinvestiments.entity.AppUser;
import com.ssdev.rsfinanceandinvestiments.service.UserService;
import com.ssdev.rsfinanceandinvestiments.utility.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService; 

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        AppUser user = userService.register(request);
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);
        return new AuthResponse(token);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtUtil.generateToken(userDetails);
        return new AuthResponse(token);
    }
    
   

}
