package org.boda.smartqueue.queue_server.Controller;

import org.boda.smartqueue.queue_server.DTO.*;
import org.boda.smartqueue.queue_server.model.userDataModel;
import org.boda.smartqueue.queue_server.services.UserService;
import org.boda.smartqueue.queue_server.JWT.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS})
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDTO>> registerUser(@Valid @RequestBody userDataModel user) {
        try {
            userDataModel registeredUser = userService.registerUser(user);
            UserDTO userDTO = new UserDTO(registeredUser);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User registered successfully", userDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Registration failed", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest request) {
        return userService.loginUser(request.getEmail(), request.getPassword())
                .map(user -> {
                    String token = jwtUtil.generateToken(user.getEmail());
                    UserDTO userDTO = new UserDTO(user);

                    return ResponseEntity.ok(
                            new LoginResponse(true, "Login successful", token, userDTO)
                    );
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new LoginResponse(false, "Invalid credentials", null, null)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(
                        ApiResponse.success("User found", new UserDTO(user))
                ))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found", "No user with ID: " + id)));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(user -> ResponseEntity.ok(
                        ApiResponse.success("User found", new UserDTO(user))
                ))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found", "No user with email: " + email)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers().stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Long id,
                                                           @RequestBody userDataModel user) {
        try {
            userDataModel updated = userService.updateUser(id, user);
            return ResponseEntity.ok(
                    ApiResponse.success("User updated successfully", new UserDTO(updated))
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Update failed", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Delete failed", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/ticket-status")
    public ResponseEntity<ApiResponse<UserDTO>> updateTicketStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request) {
        try {
            userDataModel updated = userService.updateTicketStatus(id, request.getStatus());
            return ResponseEntity.ok(
                    ApiResponse.success("Ticket status updated successfully", new UserDTO(updated))
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Update failed", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            String token = userService.generatePasswordResetToken(request.getEmail());
            return ResponseEntity.ok(
                    ApiResponse.success("Password reset token generated", token)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Failed to generate token", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            userService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success("Password reset successful", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Password reset failed", e.getMessage()));
        }
    }

    @GetMapping("/ticket-status/{status}")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getUsersByTicketStatus(@PathVariable String status) {
        List<UserDTO> users = userService.getUsersByTicketStatus(status).stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully", users)
        );
    }

    @GetMapping("/service-type/{serviceType}")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getUsersByServiceType(@PathVariable String serviceType) {
        List<UserDTO> users = userService.getUsersByServiceType(serviceType).stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully", users)
        );
    }

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("Server is running", "OK")
        );
    }


    class UpdateStatusRequest {
        private String status;
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    class ForgotPasswordRequest {
        private String email;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    class ResetPasswordRequest {
        private String token;
        private String newPassword;
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }


    // Add this rollback endpoint to your userDataController.java
    @PostMapping("/rollback")
    public ResponseEntity<?> rollbackRegistration(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required for rollback"));
            }

            // Find user by email and delete
            Optional<userDataModel> userOpt = userService.getUserByEmail(email);

            if (userOpt.isPresent()) {
                userService.deleteUser(userOpt.get().getId());
                return ResponseEntity.ok(Map.of("message", "User rollback successful for: " + email));
            } else {
                return ResponseEntity.ok(Map.of("message", "User not found, no rollback needed: " + email));
            }
        } catch (Exception e) {
            Map<String, String> error = Map.of("error", "Rollback failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

}