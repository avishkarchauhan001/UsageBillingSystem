package com.billing.usagebilling.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.billing.usagebilling.dto.CreateUserRequest;
import com.billing.usagebilling.dto.LoginRequest;
import com.billing.usagebilling.dto.LoginResponse;
import com.billing.usagebilling.dto.RegisterRequest;
import com.billing.usagebilling.entity.User;
import com.billing.usagebilling.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        validatePassword(request.getPassword());

        User user = new User(
            request.getUsername(),
            passwordEncoder.encode(request.getPassword()),
            request.getRole() != null ? request.getRole().toUpperCase() : "CUSTOMER",
            request.getSecurityQuestion(),
            request.getSecurityAnswer()
        );
        user.setUserState("Activated");

        userRepository.save(user);
    }

    public String getSecurityQuestion(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User does not exist"));
        if (user.getSecurityQuestion() == null || user.getSecurityQuestion().trim().isEmpty()) {
            throw new RuntimeException("No security question configured for this user");
        }
        return user.getSecurityQuestion();
    }

    public boolean verifySecurityAnswer(String username, String answer) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User does not exist"));
        if (user.getSecurityAnswer() == null || !user.getSecurityAnswer().trim().equalsIgnoreCase(answer.trim())) {
            throw new RuntimeException("Security answer is incorrect");
        }
        return true;
    }

    public void resetPassword(String username, String answer, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User does not exist"));
        if (user.getSecurityAnswer() == null || !user.getSecurityAnswer().trim().equalsIgnoreCase(answer.trim())) {
            throw new RuntimeException("Security answer is incorrect");
        }
        validatePassword(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public LoginResponse authenticate(LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            throw new RuntimeException("Wrong username or password");
        }

        String rawUsername = request.getUsername().trim();
        String rawPassword = request.getPassword().trim();

        if (rawUsername.isEmpty() || rawPassword.isEmpty()) {
            throw new RuntimeException("Wrong username or password");
        }

        User user = userRepository.findByUsername(rawUsername)
                .orElseGet(() -> userRepository.findAll().stream()
                        .filter(u -> u.getUsername().equalsIgnoreCase(rawUsername))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Wrong username or password")));

        // Check if user is deactivated
        if ("Deactivated".equalsIgnoreCase(user.getUserState())) {
            throw new RuntimeException("User is deactivated");
        }

        // Supports BCrypt encoded passwords alongside plain text passwords for existing records,
        // trimmed input, and case-insensitive matching fallback
        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword()) 
                || passwordEncoder.matches(rawPassword, user.getPassword())
                || request.getPassword().equals(user.getPassword())
                || rawPassword.equals(user.getPassword())
                || rawPassword.equalsIgnoreCase(user.getPassword());

        if (!matches) {
            throw new RuntimeException("Wrong username or password");
        }

        return new LoginResponse("mock-jwt-token", user.getUsername(), user.getRole());
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean matches = passwordEncoder.matches(oldPassword, user.getPassword()) 
                || oldPassword.equals(user.getPassword());

        if (!matches) {
            throw new RuntimeException("Current password is not valid");
        }

        validatePassword(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public Page<User> getUsersPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
    }

    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        validatePassword(request.getPassword());

        User user = new User(
            request.getUsername(),
            passwordEncoder.encode(request.getPassword()),
            request.getRole() != null ? request.getRole() : "ADMIN",
            request.getSecurityQuestion(),
            request.getSecurityAnswer()
        );
        user.setUserState("Activated");

        return userRepository.save(user);
    }

    public User updateUserRoleByUsername(String username, String newRole) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(newRole);
        return userRepository.save(user);
    }

    public void deleteUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User with ID " + id + " not found"));
        user.setUserState("Deactivated");
        userRepository.save(user);
    }

    public void deleteUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setUserState("Deactivated");
        userRepository.save(user);
    }

    public void activateUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User with ID " + id + " not found"));
        user.setUserState("Activated");
        userRepository.save(user);
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 1) {
            throw new RuntimeException("Password cannot be empty");
        }
        if (password.length() > 0 && password.length() < 1) {
            throw new RuntimeException("Password requirement is not matched");
        }
    }
}