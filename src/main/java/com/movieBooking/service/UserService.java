package com.movieBooking.service;

import java.util.Locale;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.movieBooking.dto.UpdateUserProfileRequest;
import com.movieBooking.model.User;
import com.movieBooking.repository.UserRepository;

// Service class for handling user-related operations.
@Service
public class UserService {

    private static final String BCRYPT_PATTERN = "^\\$2[aby]?\\$.{56}$";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Save a new user in the database.
    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User details are required.");
        }

        String name = normalizeRequiredText(user.getName(), "Name is required.");
        Integer age = validateAge(user.getAge());
        String email = normalizeEmail(user.getEmail());
        String phoneNumber = normalizePhoneNumber(user.getPhoneNumber());
        String password = validatePassword(user.getPassword());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalStateException("An account with this email already exists.");
        }

        user.setName(name);
        user.setAge(age);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    // Find a user by their ID.
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Retrieve all users from the database.
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> authenticateUser(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        String rawPassword = validatePassword(password);

        return userRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(user -> passwordMatches(user, rawPassword));
    }

    public User updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Profile details are required.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        String name = normalizeRequiredText(request.name(), "Name is required.");
        Integer age = validateAge(request.age());
        String email = normalizeEmail(request.email());
        String phoneNumber = normalizePhoneNumber(request.phoneNumber());
        String marriageStatus = normalizeMarriageStatus(request.marriageStatus());

        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, userId)) {
            throw new IllegalStateException("An account with this email already exists.");
        }

        user.setName(name);
        user.setAge(age);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setMarriageStatus(marriageStatus);
        return userRepository.save(user);
    }

    private String normalizeRequiredText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }

    private String normalizeEmail(String email) {
        return normalizeRequiredText(email, "Email is required.").toLowerCase(Locale.ROOT);
    }

    private String validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        return password;
    }

    private Integer validateAge(Integer age) {
        if (age == null || age < 1 || age > 120) {
            throw new IllegalArgumentException("Age must be between 1 and 120.");
        }

        return age;
    }

    private String normalizePhoneNumber(String phoneNumber) {
        String value = normalizeRequiredText(phoneNumber, "Phone number is required.");
        String digitsOnly = value.replaceAll("\\D", "");

        if (digitsOnly.length() < 10 || digitsOnly.length() > 15) {
            throw new IllegalArgumentException("Phone number must contain 10 to 15 digits.");
        }

        return digitsOnly;
    }

    private String normalizeMarriageStatus(String marriageStatus) {
        if (marriageStatus == null || marriageStatus.isBlank()) {
            return "Not specified";
        }

        String normalizedValue = marriageStatus.trim();
        if (normalizedValue.length() > 50) {
            throw new IllegalArgumentException("Marriage status must be 50 characters or less.");
        }

        return normalizedValue;
    }

    private boolean passwordMatches(User user, String rawPassword) {
        String storedPassword = user.getPassword();

        if (storedPassword == null || storedPassword.isBlank()) {
            return false;
        }

        if (storedPassword.matches(BCRYPT_PATTERN)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }

        if (storedPassword.equals(rawPassword)) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
            return true;
        }

        return false;
    }
}
