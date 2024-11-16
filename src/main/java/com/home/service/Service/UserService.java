package com.home.service.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.home.service.dto.AuthenticationResponse;
import com.home.service.dto.CustomerResponse;
import com.home.service.dto.LoginRequest;
import com.home.service.dto.NewPasswordRequest;
import com.home.service.dto.SignupRequest;
import com.home.service.dto.TechnicianResponse;
import com.home.service.dto.UserResponse;
import com.home.service.config.JwtUtil;
import com.home.service.config.MyUserDetailsService;
import com.home.service.config.exceptions.UserNotFoundException;
import com.home.service.models.Customer;
import com.home.service.models.PasswordResetToken;
import com.home.service.models.Technician;
import com.home.service.models.User;
import com.home.service.models.VerificationToken;
import com.home.service.models.enums.AccountStatus;
import com.home.service.models.enums.UserRole;
import com.home.service.repositories.CustomerRepository;
import com.home.service.repositories.PasswordResetTokenRepository;
import com.home.service.repositories.TechnicianRepository;
import com.home.service.repositories.UserRepository;
import com.home.service.repositories.VerificationTokenRepository;
import com.home.service.services.EmailService;

import jakarta.transaction.Transactional;

@Service
public class UserService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private CustomerRepository customerRepository;

    public User signup(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new IllegalStateException("Email already in use");
        }

        User user = new User();
        user.setName(signupRequest.getName());
        user.setEmail(signupRequest.getEmail());
        user.setPhoneNumber(signupRequest.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setRole(UserRole.USER); // default role
        user.setStatus(AccountStatus.ACTIVE); // default status

        return userRepository.save(user);
    }

    public String verifyToken(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
        if (verificationToken == null || verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Token is invalid or expired.");
        }

        // Activate user
        User user = verificationToken.getUser();
        user.setStatus(AccountStatus.ACTIVE);
        userRepository.save(user);

        return "Account verified successfully.";
    }

    public String requestPasswordReset(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user == null)
            throw new UserNotFoundException("No account associated with this email.");

        // Check if a token already exists for the user
        PasswordResetToken existingToken = passwordResetTokenRepository.findByUser(user.get());
        if (existingToken != null) {
            throw new IllegalStateException("A password reset token already exists for this user.");
        }
        emailService.sendResetPassEmail(user.get());

        return "Password reset link sent to your email.";
    }

    public String resetPassword(NewPasswordRequest newPasswordRequest) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(newPasswordRequest.getToken());
        if (resetToken == null ||
                resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Token is invalid or expired.");
        }

        // Update user password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPasswordRequest.getPassword()));
        userRepository.save(user);

        return "Password reset successfully.";
    }

    public User saveUser(User user) {
        // Encrypt the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public AuthenticationResponse authenticate(LoginRequest loginRequest) {
        System.out.println("loginRequest = " + loginRequest.getEmail());
        System.out.println("loginRequest = " + loginRequest.getPassword());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()));

        System.out.println("loginRequest.getEmail() = " + loginRequest.getEmail());
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
        final String jwtToken = jwtUtil.generateToken(userDetails.getUsername());
        final User user = userRepository.findByEmail(loginRequest.getEmail()).get();
        UserResponse userResponse = new UserResponse(user.getId(), user.getName(), user.getEmail(),
                user.getPhoneNumber(), user.getRole().name(), user.getStatus().name(), user.getProfileImage());
        System.out.println("user = " + user);
        AuthenticationResponse authenticationResponse = new AuthenticationResponse(jwtToken, userResponse);
        if (user.getRole() == UserRole.TECHNICIAN) {
            Technician technician = technicianRepository.findByUser(user).get();
            TechnicianResponse technicianResponse = new TechnicianResponse(technician);

            authenticationResponse.setTechnician(technicianResponse);

        } else if (user.getRole() == UserRole.CUSTOMER) {
            Customer customer = customerRepository.findByUser(user).get();
            CustomerResponse customerResponse = new CustomerResponse(customer.getId(), customer.getServiceHistory(),
                    customer.getSavedAddresses());

            authenticationResponse.setCustomer(customerResponse);
        }
        return authenticationResponse;
        // return new AuthenticationResponse(jwtToken, new User());
    }

    public String suspendUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        user.setStatus(AccountStatus.SUSPENDED);
        userRepository.save(user);

        return "User account suspended successfully.";
    }

    public String deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        user.setStatus(AccountStatus.DELETED);
        userRepository.save(user);

        // Optionally, you can delete the user entirely:
        // userRepository.delete(user);

        return "User account deleted successfully.";
    }

    @Transactional
    public void updateProfileImage(Long userId, String fileName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update the profile image path
        user.setProfileImage(fileName);

        // Save the updated user
        userRepository.save(user);
    }

}
