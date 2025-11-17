package org.boda.smartqueue.queue_server.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.boda.smartqueue.queue_server.DTO.*;
import org.boda.smartqueue.queue_server.Repo.ActiveQueueItemRepository;
import org.boda.smartqueue.queue_server.Repo.QueueStateRepository;
import org.boda.smartqueue.queue_server.model.ActiveQueueItem;
import org.boda.smartqueue.queue_server.model.QueueState;
import org.boda.smartqueue.queue_server.model.userDataModel;
import org.boda.smartqueue.queue_server.services.UserService;
import org.boda.smartqueue.queue_server.JWT.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.Comparator;
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

    // ADD THIS AUTOWIRED FIELD
    @Autowired
    private QueueStateRepository queueStateRepository;
    @Autowired
    private ActiveQueueItemRepository activeQueueItemRepository;
    // ... existing methods ...

    // NEW ENDPOINT: Get the current state of all queues (or specific service)
    @GetMapping("/queues/state")
    public ResponseEntity<ApiResponse<List<QueueStateDTO>>> getQueueState(
            @RequestParam(required = false) String serviceType) { // Optional parameter to filter by service

        System.out.println("🔍 AWS Server: Received request for queue state, serviceType filter: " + serviceType);

        try {
            List<QueueState> queueStates;

            if (serviceType != null && !serviceType.trim().isEmpty()) {
                // Fetch state for a specific service
                Optional<QueueState> stateOpt = queueStateRepository.findByServiceType(serviceType);
                if (stateOpt.isPresent()) {
                    queueStates = List.of(stateOpt.get());
                } else {
                    queueStates = List.of(); // Return empty list if service not found
                }
            } else {
                // Fetch state for all services
                queueStates = queueStateRepository.findAll();
            }

            // Convert entities to DTOs for the response
            List<QueueStateDTO> dtos = queueStates.stream()
                    .map(QueueStateDTO::new)
                    .collect(Collectors.toList());

            System.out.println("✅ AWS Server: Returning queue state for " + dtos.size() + " services.");
            return ResponseEntity.ok(ApiResponse.success("Queue state retrieved successfully", dtos));

        } catch (Exception e) {
            System.err.println("❌ AWS Server: Error fetching queue state: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve queue state", e.getMessage()));
        }
    }

    // ... existing methods ...

    // ADD THIS INNER DTO CLASS for the response
    public static class QueueStateDTO {
        private String serviceType;
        private String currentTicketNumber;
        private String nextTicketNumber;
        private LocalDateTime lastUpdatedAt;

        // Constructor from entity
        public QueueStateDTO(QueueState state) {
            this.serviceType = state.getServiceType();
            this.currentTicketNumber = state.getCurrentTicketNumber();
            this.nextTicketNumber = state.getNextTicketNumber();
            this.lastUpdatedAt = state.getLastUpdatedAt();
        }

        // Getters and Setters
        public String getServiceType() { return serviceType; }
        public void setServiceType(String serviceType) { this.serviceType = serviceType; }
        public String getCurrentTicketNumber() { return currentTicketNumber; }
        public void setCurrentTicketNumber(String currentTicketNumber) { this.currentTicketNumber = currentTicketNumber; }
        public String getNextTicketNumber() { return nextTicketNumber; }
        public void setNextTicketNumber(String nextTicketNumber) { this.nextTicketNumber = nextTicketNumber; }
        public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
        public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
    }


//***************************************************************************



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








    // NEW ENDPOINT: Get the list of active tickets for ALL services


    // NEW ENDPOINT: Allow local server to ADD an active ticket to AWS
    @PostMapping("/queues/active")
    public ResponseEntity<ApiResponse<ActiveQueueItemDTO>> addActiveTicketToAws(
            @RequestBody AddActiveTicketRequest request) {

        System.out.println("🔧 AWS Server: Received request to add active ticket to AWS for email: " + request.getEmail() + ", service: " + request.getServiceType() + ", number: " + request.getCustomerNumber());

        try {
            // Check if the item already exists to avoid duplicates
            ActiveQueueItem existingItem = activeQueueItemRepository.findByUserEmailAndServiceType(request.getEmail(), request.getServiceType());
            if (existingItem != null) {
                System.out.println("⚠️ AWS Server: Active ticket already exists for user: " + request.getEmail() + " in service: " + request.getServiceType());
                // You could choose to update the existing one or return an error
                // For now, let's update it.
                existingItem.setCustomerNumber(request.getCustomerNumber());
                existingItem.setLastUpdatedAt(LocalDateTime.now());
                ActiveQueueItem updatedItem = activeQueueItemRepository.save(existingItem);
                return ResponseEntity.ok(ApiResponse.success("Active ticket updated on AWS", new ActiveQueueItemDTO(updatedItem)));
            }

            // Create new active queue item
            ActiveQueueItem newItem = new ActiveQueueItem(request.getEmail(), request.getServiceType(), request.getCustomerNumber());
            ActiveQueueItem savedItem = activeQueueItemRepository.save(newItem);
            System.out.println("✅ AWS Server: Added active ticket for user: " + request.getEmail() + " to service: " + request.getServiceType());

            return ResponseEntity.ok(ApiResponse.success("Active ticket added to AWS", new ActiveQueueItemDTO(savedItem)));

        } catch (Exception e) {
            System.err.println("❌ AWS Server: Error adding active ticket to AWS: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to add active ticket to AWS", e.getMessage()));
        }
    }

    // NEW ENDPOINT: Allow local server to REMOVE an active ticket from AWS (on cancel/complete)
    @DeleteMapping("/queues/active")
    public ResponseEntity<ApiResponse<String>> removeActiveTicketFromAws(
            @RequestBody RemoveActiveTicketRequest request) {

        System.out.println("🔧 AWS Server: Received request to remove active ticket from AWS for email: " + request.getEmail() + ", service: " + request.getServiceType());

        try {
            // Find and delete the specific item
            activeQueueItemRepository.deleteByUserEmailAndServiceType(request.getEmail(), request.getServiceType());
            System.out.println("✅ AWS Server: Removed active ticket for user: " + request.getEmail() + " from service: " + request.getServiceType());

            return ResponseEntity.ok(ApiResponse.success("Active ticket removed from AWS", "Ticket removed successfully"));

        } catch (Exception e) {
            System.err.println("❌ AWS Server: Error removing active ticket from AWS: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to remove active ticket from AWS", e.getMessage()));
        }
    }

    @PostMapping("/ticket-status") // Previously might have been @PatchMapping
    public ResponseEntity<ApiResponse<UserDTO>> updateTicketStatusFromLocalServer(
            @RequestBody UpdateTicketInfoRequest request) { // Use the DTO defined below

        System.out.println("🔧 AWS Server: Received ticket info update request from local server for email: " + request.getEmail());

        try {
            // Find the user on AWS by email
            Optional<userDataModel> userOpt = userService.getUserByEmail(request.getEmail());

            if (userOpt.isEmpty()) {
                System.out.println("❌ AWS Server: User not found for email: " + request.getEmail());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found", "No user with email: " + request.getEmail()));
            }

            userDataModel user = userOpt.get();

            // Update the user's ticket-related fields based on the request
            // Validate the incoming data (e.g., check if status is allowed: ACTIVE, CANCELLED, COMPLETED)
            if (request.getCustomerNumber() != null) {
                user.setCustomerNumber(request.getCustomerNumber()); // This should be a String now if changed on AWS model
            }
            if (request.getServiceType() != null) {
                user.setServiceType(request.getServiceType());
            }
            if (request.getTicketStatus() != null) {
                // Optional: Add validation for allowed status transitions
                // e.g., only allow ACTIVE->CANCELLED, not CANCELLED->ACTIVE directly from local server
                user.setTicketStatus(request.getTicketStatus());
            }
            // Note: We probably don't want the local server to update name, email, password, etc.
            // Consider adding validation here for allowed status transitions (e.g., ACTIVE -> CANCELLED)

            user.setUpdatedAt(LocalDateTime.now()); // Update the timestamp

            // Save the updated user back to the AWS database
            userDataModel updatedUser = userService.updateUser(user.getId(), user); // Assuming your update method works with the full object
            System.out.println("✅ AWS Server: Updated ticket info for user: " + updatedUser.getEmail());

            // Return the updated user DTO
            return ResponseEntity.ok(ApiResponse.success("Ticket info updated successfully", new UserDTO(updatedUser)));

        } catch (Exception e) {
            System.err.println("❌ AWS Server: Error updating ticket info: " + e.getMessage());
            e.printStackTrace(); // Log the full stack trace for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Update failed", e.getMessage()));
        }
    }


    @GetMapping("/queues/active")
    public ResponseEntity<ApiResponse<List<ActiveQueueItemDTO>>> getActiveQueues(
            HttpServletRequest request) { // Add HttpServletRequest to access headers

        // Extract the API key from the request header
        String apiKey = request.getHeader("X-API-Key");
        String key="221008874";
        // Validate the API key against the expected local server key
        if (!key.equals(apiKey)) { // Use the same constant defined in your SecurityConfig or a utility class
            System.err.println("❌ AWS Server: Unauthorized access attempt to /queues/active. Provided API Key: " + apiKey);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Unauthorized", "Invalid API Key provided."));
        }

        System.out.println("🔍 AWS Server: Received request for all active queues with valid API key");

        try {
            // Fetch all active queue items, potentially group by service type in the response
            List<ActiveQueueItem> allActiveItems = activeQueueItemRepository.findAll();

            // Group by service type and sort within each group
            Map<String, List<ActiveQueueItemDTO>> groupedItems = allActiveItems.stream()
                    .map(ActiveQueueItemDTO::new)
                    .sorted(Comparator.comparing(ActiveQueueItemDTO::getCustomerNumber)) // Sort by ticket number
                    .collect(Collectors.groupingBy(ActiveQueueItemDTO::getServiceType));

            // Convert the map values (lists) back to a single flat list or a structured response
            List<ActiveQueueItemDTO> dtos = groupedItems.values().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            System.out.println("✅ AWS Server: Returning " + dtos.size() + " active queue items.");
            return ResponseEntity.ok(ApiResponse.success("Active queues retrieved successfully", dtos));

        } catch (Exception e) {
            System.err.println("❌ AWS Server: Error fetching active queues: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve active queues", e.getMessage()));
        }
    }

    // NEW ENDPOINT: Get the list of active tickets for a specific service
    // ALSO REQUIRE API KEY for this endpoint
    @GetMapping("/queues/active/{serviceType}")
    public ResponseEntity<ApiResponse<List<ActiveQueueItemDTO>>> getActiveQueueForService(

            @PathVariable String serviceType,
            HttpServletRequest request) { // Add HttpServletRequest to access headers

        // Extract and validate the API key
        String key="221008874";
        String apiKey = request.getHeader("X-API-Key");
        if (!key.equals(apiKey)) {
            System.err.println("❌ AWS Server: Unauthorized access attempt to /queues/active/$serviceType. Provided API Key: " + apiKey);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Unauthorized", "Invalid API Key provided."));
        }

        System.out.println("🔍 AWS Server: Received request for active queue for service: " + serviceType);

        try {
            List<ActiveQueueItem> activeItems = activeQueueItemRepository.findByServiceTypeOrderByCustomerNumberAsc(serviceType);
            List<ActiveQueueItemDTO> dtos = activeItems.stream()
                    .map(ActiveQueueItemDTO::new)
                    .collect(Collectors.toList());

            System.out.println("✅ AWS Server: Returning " + dtos.size() + " active queue items for service: " + serviceType);
            return ResponseEntity.ok(ApiResponse.success("Active queue for service retrieved successfully", dtos));

        } catch (Exception e) {
            System.err.println("❌ AWS Server: Error fetching active queue for service " + serviceType + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve active queue for service", e.getMessage()));
        }
    }

    public static class ActiveQueueItemDTO {
        private String userEmail;
        private String serviceType;
        private String customerNumber;
        private LocalDateTime assignedAt;
        private LocalDateTime lastUpdatedAt;

        // Constructor from entity
        public ActiveQueueItemDTO(ActiveQueueItem item) {
            this.userEmail = item.getUserEmail();
            this.serviceType = item.getServiceType();
            this.customerNumber = item.getCustomerNumber();
            this.assignedAt = item.getAssignedAt();
            this.lastUpdatedAt = item.getLastUpdatedAt();
        }

        // Getters and Setters
        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
        public String getServiceType() { return serviceType; }
        public void setServiceType(String serviceType) { this.serviceType = serviceType; }
        public String getCustomerNumber() { return customerNumber; }
        public void setCustomerNumber(String customerNumber) { this.customerNumber = customerNumber; }
        public LocalDateTime getAssignedAt() { return assignedAt; }
        public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
        public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
        public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
    }

    // ADD THE REQUEST DTO CLASS for adding active ticket
    public static class AddActiveTicketRequest {
        private String email;
        private String serviceType;
        private String customerNumber;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getServiceType() { return serviceType; }
        public void setServiceType(String serviceType) { this.serviceType = serviceType; }
        public String getCustomerNumber() { return customerNumber; }
        public void setCustomerNumber(String customerNumber) { this.customerNumber = customerNumber; }
    }

    // ADD THE REQUEST DTO CLASS for removing active ticket
    public static class RemoveActiveTicketRequest {
        private String email;
        private String serviceType;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getServiceType() { return serviceType; }
        public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    }



    // ... existing methods ...

    // ADD THIS INNER REQUEST DTO CLASS for the update endpoint
    public static class UpdateQueueStateRequest {
        private String serviceType;
        private String currentTicketNumber;
        private String nextTicketNumber;

        // Getters and Setters
        public String getServiceType() { return serviceType; }
        public void setServiceType(String serviceType) { this.serviceType = serviceType; }
        public String getCurrentTicketNumber() { return currentTicketNumber; }
        public void setCurrentTicketNumber(String currentTicketNumber) { this.currentTicketNumber = currentTicketNumber; }
        public String getNextTicketNumber() { return nextTicketNumber; }
        public void setNextTicketNumber(String nextTicketNumber) { this.nextTicketNumber = nextTicketNumber; }
    }
    public static class UpdateTicketInfoRequest {
        private String email;
        private String customerNumber; // Should be String to match your changes
        private String serviceType;
        private String ticketStatus;

        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getCustomerNumber() { return customerNumber; }
        public void setCustomerNumber(String customerNumber) { this.customerNumber = customerNumber; }
        public String getServiceType() { return serviceType; }
        public void setServiceType(String serviceType) { this.serviceType = serviceType; }
        public String getTicketStatus() { return ticketStatus; }
        public void setTicketStatus(String ticketStatus) { this.ticketStatus = ticketStatus; }
    }

}