package com.boatsafari.backend.service;

import com.boatsafari.backend.entity.User;
import com.boatsafari.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.boatsafari.backend.dto.LoginRequest;
import com.boatsafari.backend.dto.LoginResponse;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

        private final UserRepository userRepository;
        private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        private final JwtService jwtService;

        public UserService(
                        UserRepository userRepository,
                        JwtService jwtService) {

                this.userRepository = userRepository;
                this.jwtService = jwtService;
        }

        // Login user
        public LoginResponse login(LoginRequest loginRequest) {

                if (loginRequest.getEmail() == null ||
                                loginRequest.getEmail().isBlank()) {

                        throw new RuntimeException(
                                        "Email is required");
                }
                if (loginRequest.getPassword() == null ||
                                loginRequest.getPassword().isBlank()) {

                        throw new RuntimeException(
                                        "Password is required");
                }

                User user = userRepository.findByEmailIgnoreCase(
                                loginRequest.getEmail())
                                .orElseThrow(() -> new RuntimeException(
                                                "Invalid email or password"));

                if (!passwordEncoder.matches(
                                loginRequest.getPassword(),
                                user.getPassword())) {

                        throw new RuntimeException(
                                        "Invalid email or password");
                }

                String token = jwtService.generateToken(
                                user.getEmail(),
                                user.getRole());

                LoginResponse response = new LoginResponse(
                                user.getId(),
                                user.getName(),
                                user.getEmail(),
                                user.getRole());

                response.setToken(token);

                return response;
        }

        // Check whether JWT belongs to a manager
        public void validateManagerToken(String token) {

                if (!jwtService.isTokenValid(token)) {
                        throw new RuntimeException(
                                        "Invalid or expired token");
                }

                String role = jwtService.getRoleFromToken(token);

                if (!"MANAGER".equalsIgnoreCase(role)) {
                        throw new RuntimeException(
                                        "Manager access required");
                }
        }

        // Get currently logged-in user from JWT
        public User getCurrentUser(String token) {

                if (!jwtService.isTokenValid(token)) {
                        throw new RuntimeException(
                                        "Invalid or expired token");
                }

                String email = jwtService.getEmailFromToken(token);

                return userRepository.findByEmailIgnoreCase(email)
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found"));
        }

        // Create a new user
        public User createUser(User user) {

                if (user.getName() == null ||
                                user.getName().isBlank()) {

                        throw new RuntimeException(
                                        "Name is required");
                }
                if (user.getPhoneNumber() == null ||
                                user.getPhoneNumber().isBlank()) {

                        throw new RuntimeException(
                                        "Phone number is required");
                }
                if (user.getPassword() == null ||
                                user.getPassword().isBlank()) {

                        throw new RuntimeException(
                                        "Password is required");
                }
                if (user.getEmail() == null ||
                                user.getEmail().isBlank()) {

                        throw new RuntimeException(
                                        "Email is required");
                }
                // Public registration always creates a CUSTOMER
                user.setRole("CUSTOMER");

                if (userRepository.existsByEmailIgnoreCase(
                                user.getEmail())) {

                        throw new RuntimeException(
                                        "Email already exists");
                }
                user.setPassword(
                                passwordEncoder.encode(
                                                user.getPassword()));
                return userRepository.save(user);
        }

        // Get all users
        public List<User> getAllUsers() {
                return userRepository.findAll();
        }

        // Get user by ID
        public Optional<User> getUserById(Long id) {
                return userRepository.findById(id);
        }

        public User getUserForUser(
                        Long userId,
                        User authenticatedUser) {

                User user = userRepository
                                .findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Manager can view any user
                if ("MANAGER".equalsIgnoreCase(
                                authenticatedUser.getRole())) {

                        return user;
                }

                // Customer can view only their own account
                if (user.getId()
                                .equals(authenticatedUser.getId())) {

                        return user;
                }

                throw new RuntimeException(
                                "You can only access your own account");
        }

        // Update an existing user
        public User updateUser(
                        Long id,
                        User updatedUser,
                        User authenticatedUser) {

                User existingUser = userRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                boolean isManager = "MANAGER".equalsIgnoreCase(
                                authenticatedUser.getRole());

                boolean isOwner = existingUser.getId()
                                .equals(authenticatedUser.getId());

                if (!isManager && !isOwner) {
                        throw new RuntimeException(
                                        "You can only update your own account");
                }

                if (updatedUser.getName() == null ||
                                updatedUser.getName().isBlank()) {

                        throw new RuntimeException(
                                        "Name is required");
                }
                if (updatedUser.getPhoneNumber() == null ||
                                updatedUser.getPhoneNumber().isBlank()) {

                        throw new RuntimeException(
                                        "Phone number is required");
                }

                if (updatedUser.getEmail() == null ||
                                updatedUser.getEmail().isBlank()) {

                        throw new RuntimeException(
                                        "Email is required");
                }

                if (userRepository.existsByEmailIgnoreCaseAndIdNot(
                                updatedUser.getEmail(),
                                id)) {

                        throw new RuntimeException(
                                        "Email already exists");
                }
                existingUser.setName(updatedUser.getName());
                existingUser.setEmail(updatedUser.getEmail());

                if (updatedUser.getPassword() != null &&
                                !updatedUser.getPassword().isBlank()) {

                        existingUser.setPassword(
                                        passwordEncoder.encode(
                                                        updatedUser.getPassword()));
                }

                existingUser.setPhoneNumber(updatedUser.getPhoneNumber());

                if (updatedUser.getRole() == null ||
                                updatedUser.getRole().isBlank()) {

                        throw new RuntimeException(
                                        "Role is required");
                }

                String role = updatedUser.getRole().toUpperCase();

                if (!role.equals("CUSTOMER") &&
                                !role.equals("MANAGER")) {

                        throw new RuntimeException(
                                        "Invalid user role");
                }

                // Only a MANAGER can change user roles
                if (!isManager &&
                                !role.equalsIgnoreCase(existingUser.getRole())) {

                        throw new RuntimeException(
                                        "You cannot change your own role");
                }

                existingUser.setRole(role);

                return userRepository.save(existingUser);
        }

        // Delete a user
        public void deleteUser(
                        Long id,
                        User authenticatedUser) {

                User user = userRepository
                                .findById(id)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                boolean isManager = "MANAGER".equalsIgnoreCase(
                                authenticatedUser.getRole());

                boolean isOwner = user.getId()
                                .equals(authenticatedUser.getId());

                if (!isManager && !isOwner) {
                        throw new RuntimeException(
                                        "You can only delete your own account");
                }

                userRepository.delete(user);
        }
}