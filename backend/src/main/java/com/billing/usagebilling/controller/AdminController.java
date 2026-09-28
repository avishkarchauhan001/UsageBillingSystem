package com.billing.usagebilling.controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.billing.usagebilling.dto.CreateUserRequest;
import com.billing.usagebilling.dto.UpdateRoleRequest;
import com.billing.usagebilling.entity.User;
import com.billing.usagebilling.service.UserService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserService userService;
    private final com.billing.usagebilling.service.PlanService planService;

    public AdminController(UserService userService, com.billing.usagebilling.service.PlanService planService) {
        this.userService = userService;
        this.planService = planService;
    }

    /**
     * SRS US07: Admin Plan Report / Sales pictorial analysis
     */
    @GetMapping("/report")
    public ResponseEntity<java.util.List<com.billing.usagebilling.dto.PlanReportDto>> getSalesReport() {
        return ResponseEntity.ok(planService.getPlanSalesReport());
    }

    @GetMapping("/users")
    public ResponseEntity<Page<User>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(userService.getUsersPaginated(page, size));
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        return new ResponseEntity<>(userService.createUser(request), HttpStatus.CREATED);
    }

    @PutMapping("/users/role")
    public ResponseEntity<User> updateUserRole(@RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(userService.updateUserRoleByUsername(request.getUsername(), request.getRole()));
    }

    // ADDED: Handles DELETE http://localhost:8081/api/admin/users/{id}
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    // Retained: Handles DELETE http://localhost:8081/api/admin/users?username=admin
    @DeleteMapping("/users")
    public ResponseEntity<?> deleteUserByUsername(@RequestParam String username) {
        userService.deleteUserByUsername(username);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }
}