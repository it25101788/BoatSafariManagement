package com.boatsafari.backend.controller;

import com.boatsafari.backend.entity.User;
import com.boatsafari.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.boatsafari.backend.dto.LoginRequest;
import com.boatsafari.backend.dto.LoginResponse;

import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private User getAuthenticatedUser(
            String authorizationHeader) {

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            throw new RuntimeException(
                    "Authorization token is required");
        }

        String token = authorizationHeader
                .substring(7)
                .trim();

        return userService.getCurrentUser(token);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(
            RuntimeException e) {

        return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage()));
    }

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest loginRequest) {

        return userService.login(loginRequest);
    }

    // Create a new user
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    // Get all users
    // Get all users - MANAGER only
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            throw new RuntimeException(
                    "Authorization token is required");
        }

        String token = authorizationHeader.substring(7).trim();

        userService.validateManagerToken(token);

        return ResponseEntity.ok(
                userService.getAllUsers());
    }

    // Get user by ID
    // Get user by ID - own account or MANAGER
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        User authenticatedUser = getAuthenticatedUser(authorizationHeader);

        User user = userService.getUserForUser(
                id,
                authenticatedUser);

        return ResponseEntity.ok(user);
    }

    // Get currently logged-in user
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            throw new RuntimeException(
                    "Authorization token is required");
        }

        String token = authorizationHeader.substring(7).trim();

        User user = userService.getCurrentUser(token);

        return ResponseEntity.ok(user);
    }

    // Update user

    // Update user - own account or MANAGER
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody User updatedUser) {

        User authenticatedUser = getAuthenticatedUser(authorizationHeader);

        User user = userService.updateUser(
                id,
                updatedUser,
                authenticatedUser);

        return ResponseEntity.ok(user);
    }

    // Delete user
    // Delete user - own account or MANAGER
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        User authenticatedUser = getAuthenticatedUser(authorizationHeader);

        userService.deleteUser(
                id,
                authenticatedUser);

        return ResponseEntity.noContent().build();
    }
}
