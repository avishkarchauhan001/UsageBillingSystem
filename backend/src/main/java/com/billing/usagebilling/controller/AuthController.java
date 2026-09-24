package com.billing.usagebilling.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.billing.usagebilling.dto.LoginRequest;
import com.billing.usagebilling.dto.LoginResponse;
import com.billing.usagebilling.dto.RegisterRequest;
import com.billing.usagebilling.dto.ChangePasswordRequest;
import com.billing.usagebilling.service.UserService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = userService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody ChangePasswordRequest request) {
        userService.changePassword(
            request.getUsername(),
            request.getOldPassword(),
            request.getNewPassword()
        );
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @GetMapping("/security-question/{username}")
    public ResponseEntity<Map<String, String>> getSecurityQuestion(@PathVariable String username) {
        String question = userService.getSecurityQuestion(username);
        return ResponseEntity.ok(Map.of("securityQuestion", question));
    }

    @PostMapping("/forgot-password/verify")
    public ResponseEntity<Map<String, Object>> verifySecurityAnswer(@RequestBody com.billing.usagebilling.dto.ForgotPasswordVerifyRequest request) {
        boolean valid = userService.verifySecurityAnswer(request.getUsername(), request.getSecurityAnswer());
        return ResponseEntity.ok(Map.of("valid", valid, "message", "Security answer verified successfully"));
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody com.billing.usagebilling.dto.ForgotPasswordResetRequest request) {
        userService.resetPassword(request.getUsername(), request.getSecurityAnswer(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully. Please login again."));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }
}