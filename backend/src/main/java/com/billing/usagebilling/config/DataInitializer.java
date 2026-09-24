package com.billing.usagebilling.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.billing.usagebilling.dto.CreateUserRequest;
import com.billing.usagebilling.repository.UserRepository;
import com.billing.usagebilling.service.UserService;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final UserRepository userRepository;

    public DataInitializer(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByUsername("admin")) {
            CreateUserRequest admin = new CreateUserRequest();
            admin.setUsername("admin");
            admin.setPassword("1"); // Default admin password
            admin.setRole("ADMIN");
            admin.setSecurityQuestion("What is your pet name?");
            admin.setSecurityAnswer("admin");
            userService.createUser(admin);
            System.out.println(">>> Default admin user created successfully! <<<");
        }
    }
}