// File: org/boda/smartqueue/queue_server/services/UserService.java
package org.boda.smartqueue.queue_server.services;

import org.boda.smartqueue.queue_server.model.userDataModel;
import org.boda.smartqueue.queue_server.Repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    @Transactional
    public userDataModel registerUser(userDataModel user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new RuntimeException("Phone number already registered");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // ✅ GENERATE STRING CUSTOMER NUMBER - Update generation logic if necessary
        // For example, generate as a string like "L001", "B002", etc., or just a padded integer string
        // For now, assuming it's just a unique integer converted to string
        user.setCustomerNumber(generateUniqueCustomerNumberString()); // Call new method
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setTicketStatus("INACTIVE");

        return userRepository.save(user);
    }

    // ✅ NEW METHOD: Generate unique customer number as String
    private String generateUniqueCustomerNumberString() {
        String customerNumber;
        do {
            // Generate a random 6-digit number as a string
            customerNumber = String.format("%06d", 100000 + random.nextInt(900000));
        } while (userRepository.existsByCustomerNumber(customerNumber)); // Use String version of exists check

        return customerNumber;
    }

    public Optional<userDataModel> loginUser(String email, String password) {
        Optional<userDataModel> user = userRepository.findByEmail(email);

        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            // ✅ VALIDATION: Ensure email is not null after login
            if (user.get().getEmail() == null || user.get().getEmail().isEmpty()) {
                System.err.println("❌ SECURITY ALERT: User object loaded from DB has null/empty email for ID: " + user.get().getId());
                return Optional.empty(); // Reject login if email is missing
            }
            return user;
        }

        return Optional.empty();
    }

    public Optional<userDataModel> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<userDataModel> getUserByEmail(String email) {
        Optional<userDataModel> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            userDataModel user = userOpt.get();
            // ✅ VALIDATION: Ensure email is not null
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                System.err.println("❌ SECURITY ALERT: User object found in DB has null/empty email for requested email: " + email);
                return Optional.empty(); // Return empty if validation fails
            }
            System.out.println("✅ UserService: Found user in DB. Email: '" + user.getEmail() + "', ticket: '" + user.getCustomerNumber() + "', status: '" + user.getTicketStatus() + "'");
        }
        return userOpt;
    }

    public List<userDataModel> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public userDataModel updateUser(Long id, userDataModel updatedUser) {
        userDataModel user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check email uniqueness (excluding current user)
        if (updatedUser.getEmail() != null && !user.getEmail().equals(updatedUser.getEmail()) &&
                userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new RuntimeException("Email already exists: " + updatedUser.getEmail());
        }

        // ✅ CHECK CUSTOMER NUMBER UNIQUENESS (excluding current user) - Now handles String
        if (updatedUser.getCustomerNumber() != null &&
                !updatedUser.getCustomerNumber().equals(user.getCustomerNumber()) &&
                userRepository.existsByCustomerNumber(updatedUser.getCustomerNumber())) { // Use String version
            throw new RuntimeException("Customer number already exists: " + updatedUser.getCustomerNumber());
        }

        if (updatedUser.getName() != null) user.setName(updatedUser.getName());
        if (updatedUser.getEmail() != null) user.setEmail(updatedUser.getEmail()); // Update email if provided
        if (updatedUser.getServiceType() != null) user.setServiceType(updatedUser.getServiceType());
        if (updatedUser.getPhoneNumber() != null) user.setPhoneNumber(updatedUser.getPhoneNumber());
        if (updatedUser.getCustomerNumber() != null) user.setCustomerNumber(updatedUser.getCustomerNumber()); // Update customer number if provided

        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public userDataModel updateTicketStatus(Long id, String status) {
        userDataModel user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setTicketStatus(status);
        user.setUpdatedAt(LocalDateTime.now());

        if ("COMPLETED".equalsIgnoreCase(status)) {
            user.setServiceCompletedAt(LocalDateTime.now());
        } else if ("CANCELLED".equalsIgnoreCase(status)) {
            user.setServiceCancelledAt(LocalDateTime.now());
        }

        return userRepository.save(user);
    }

    @Transactional
    public String generatePasswordResetToken(String email) {
        userDataModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordExpiresAt(LocalDateTime.now().plusHours(1));

        userRepository.save(user);
        return token;
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        userDataModel user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (user.getResetPasswordExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpiresAt(null);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    // Removed the old generateUniqueCustomerNumber method as we now use String

    public List<userDataModel> getUsersByTicketStatus(String status) {
        return userRepository.findByTicketStatus(status);
    }

    public List<userDataModel> getUsersByServiceType(String serviceType) {
        return userRepository.findByServiceType(serviceType);
    }
}