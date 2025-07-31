package com.ssdev.rsfinanceandinvestiments.service;

import com.ssdev.rsfinanceandinvestiments.dto.RegisterRequest;
import com.ssdev.rsfinanceandinvestiments.entity.AppUser;
import com.ssdev.rsfinanceandinvestiments.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    // Store OTPs temporarily
    private final Map<String, String> otpStore = new HashMap<>();

    // Store new passwords temporarily until OTP is verified
    private final Map<String, String> pendingPasswords = new HashMap<>();

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // === REGISTER ===
    public AppUser register(RegisterRequest request) {
        AppUser user = new AppUser();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userRepository.save(user);
    }

    // === Generate and Send OTP ===
    public String generateAndSendOtp(String email) {
        String otp = String.valueOf((int) ((Math.random() * 900000) + 100000)); // 6-digit OTP
        otpStore.put(email, otp);

        // Send email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP for Password Reset");
        message.setText("Your OTP code is: " + otp + "\nThis OTP is valid for 10 minutes.");
        mailSender.send(message);

        System.out.println("OTP for " + email + ": " + otp); // Optional debug log
        return otp;
    }

    // === Verify OTP ===
    public boolean verifyOtp(String email, String enteredOtp) {
        String validOtp = otpStore.get(email);
        System.out.print(validOtp+"valid ot");
        if (validOtp != null && validOtp.equals(enteredOtp)) {
            return true;
        }
        return false;
    }

    // === Store password temporarily ===
    public void storePendingPassword(String email, String rawPassword) {
        String encoded = passwordEncoder.encode(rawPassword);
        pendingPasswords.put(email, encoded);
    }

    // === Final step: save password after OTP is verified ===
    public void commitPendingPassword(String email) {
        String encodedPassword = pendingPasswords.get(email);
        if (encodedPassword == null) {
            throw new IllegalStateException("No pending password for user: " + email);
        }

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setPassword(encodedPassword);
        userRepository.save(user);

        // Cleanup
        pendingPasswords.remove(email);
        otpStore.remove(email);
    }

    // (Optional) Force update password without OTP
    public void updatePassword(String email, String newPassword) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
