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
        user.setCustomerNumber(generateUniqueCustomerNumber());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setTicketStatus("INACTIVE");

        return userRepository.save(user);
    }

    public Optional<userDataModel> loginUser(String email, String password) {
        Optional<userDataModel> user = userRepository.findByEmail(email);

        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            return user;
        }

        return Optional.empty();
    }

    public Optional<userDataModel> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<userDataModel> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<userDataModel> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public userDataModel updateUser(Long id, userDataModel updatedUser) {
        userDataModel user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updatedUser.getName() != null) user.setName(updatedUser.getName());
        if (updatedUser.getServiceType() != null) user.setServiceType(updatedUser.getServiceType());
        if (updatedUser.getPhoneNumber() != null) user.setPhoneNumber(updatedUser.getPhoneNumber());

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

    private Integer generateUniqueCustomerNumber() {
        Integer customerNumber;
        do {
            customerNumber = 100000 + random.nextInt(900000);
        } while (userRepository.findByCustomerNumber(customerNumber).isPresent());

        return customerNumber;
    }

    public List<userDataModel> getUsersByTicketStatus(String status) {
        return userRepository.findByTicketStatus(status);
    }

    public List<userDataModel> getUsersByServiceType(String serviceType) {
        return userRepository.findByServiceType(serviceType);
    }
}