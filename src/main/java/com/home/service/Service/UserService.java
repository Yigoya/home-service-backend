package com.home.service.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.home.service.dto.AuthenticationResponse;
import com.home.service.dto.ChangeContactRequest;
import com.home.service.dto.CustomerResponse;
import com.home.service.dto.LoginRequest;
import com.home.service.dto.NewPasswordRequest;
import com.home.service.dto.SignupRequest;
import com.home.service.dto.SocialLoginRequest;
import com.home.service.dto.TechnicianProfileDTO;
import com.home.service.dto.TechnicianResponse;
import com.home.service.dto.UserResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.home.service.config.JwtUtil;
import com.home.service.config.MyUserDetailsService;
import com.home.service.config.exceptions.UserNotFoundException;
import com.home.service.models.CustomDetails;
import com.home.service.models.Customer;
import com.home.service.models.DeviceInfo;
import com.home.service.models.PasswordResetToken;
import com.home.service.models.Technician;
import com.home.service.models.User;
import com.home.service.models.VerificationToken;
import com.home.service.models.enums.AccountStatus;
import com.home.service.models.enums.EthiopianLanguage;
import com.home.service.models.enums.UserRole;
import com.home.service.repositories.CustomerRepository;
import com.home.service.repositories.PasswordResetTokenRepository;
import com.home.service.repositories.TechnicianRepository;
import com.home.service.repositories.UserRepository;
import com.home.service.repositories.VerificationTokenRepository;
import com.home.service.services.EmailService;

import jakarta.persistence.EntityNotFoundException;
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

    @Autowired
    private VerificationTokenRepository tokenRepository;

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

    @Transactional
    public Map<String, Object> verifyToken(String token) {
        System.out.println("Token: " + token);
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("Token is invalid or expired."));
        System.out.println(verificationToken.getExpiryDate());
        // if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
        // throw new IllegalStateException("Token is invalid or expired.");
        // }

        Technician technician = technicianRepository.findByUser(verificationToken.getUser())
                .orElseThrow(() -> new IllegalStateException("Technician not found"));

        technician.setVerified(true);
        technicianRepository.save(technician);
        TechnicianProfileDTO technicianProfileDTO = new TechnicianProfileDTO(technician,
                verificationToken.getUser().getPreferredLanguage());
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Account verified successfully.");
        response.put("technician", technicianProfileDTO);

        return response;
    }

    public String requestPasswordReset(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user == null)
            throw new UserNotFoundException("No account associated with this email.");

        // Check if a token already exists for the user
        PasswordResetToken existingToken = passwordResetTokenRepository.findByUser(user.get());
        if (existingToken != null) {
            passwordResetTokenRepository.delete(existingToken);
        }
        emailService.sendResetPassEmail(user.get());

        return "Password reset link sent to your email.";
    }

    public String resetPassword(NewPasswordRequest newPasswordRequest) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findFirstByTokenOrderByExpiryDateDesc(newPasswordRequest.getToken());
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

    @Transactional
    public AuthenticationResponse authenticate(LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()));

        CustomDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
        String jwtToken = jwtUtil.generateToken(userDetails.getEmail());
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(
                () -> new UserNotFoundException("User not found"));
        DeviceInfo deviceInfo = user.getDevices().stream()
                .filter(device -> device.getFCMToken().equals(loginRequest.getFCMToken()))
                .findFirst().orElse(null);
        if (deviceInfo != null) {
            deviceInfo.setDeviceType(loginRequest.getDeviceType());
            deviceInfo.setDeviceModel(loginRequest.getDeviceModel());
            deviceInfo.setOperatingSystem(loginRequest.getOperatingSystem());
        } else {
            deviceInfo = new DeviceInfo();
            deviceInfo.setFCMToken(loginRequest.getFCMToken());
            deviceInfo.setDeviceType(loginRequest.getDeviceType());
            deviceInfo.setDeviceModel(loginRequest.getDeviceModel());
            deviceInfo.setOperatingSystem(loginRequest.getOperatingSystem());
            user.addDevice(deviceInfo);
        }

        userRepository.save(user);

        UserResponse userResponse = new UserResponse(
                user.getId(), user.getName(), user.getEmail(),
                user.getPhoneNumber(), user.getRole().name(),
                user.getStatus().name(), user.getProfileImage(), user.getPreferredLanguage());

        AuthenticationResponse authenticationResponse = new AuthenticationResponse(jwtToken, userResponse);

        if (user.getRole() == UserRole.TECHNICIAN) {
            Technician technician = technicianRepository.findByUser(user).orElseThrow(
                    () -> new UserNotFoundException("Technician not found"));
            TechnicianResponse technicianResponse = new TechnicianResponse(technician, user.getPreferredLanguage());
            authenticationResponse.setTechnician(technicianResponse);
        } else if (user.getRole() == UserRole.CUSTOMER) {
            Customer customer = customerRepository.findByUser(user).orElseThrow(
                    () -> new UserNotFoundException("Customer not found"));
            CustomerResponse customerResponse = new CustomerResponse(
                    customer.getId(), customer.getServiceHistory(), customer.getSavedAddresses());
            authenticationResponse.setCustomer(customerResponse);
        }

        return authenticationResponse;
    }

    @Transactional
    public AuthenticationResponse handleSocialLogin(SocialLoginRequest loginRequest) throws FirebaseAuthException {
        System.out.println(loginRequest.getIdToken());
        FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(loginRequest.getIdToken());

        String email = firebaseToken.getEmail();
        String name = (String) firebaseToken.getClaims().get("name");
        String providerId = firebaseToken.getUid();

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            if (name == null) {
                name = email.split("@")[0];
            }
            user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setAuthProvider(loginRequest.getProvider());
            user.setProviderId(providerId);
            user.setPassword(providerId);
            user.setRole(UserRole.CUSTOMER);
            user.setStatus(AccountStatus.ACTIVE);
            user.setPhoneNumber(
                    loginRequest.getPhoneNumber() == null ? "No phone number" : loginRequest.getPhoneNumber());
            userRepository.save(user);
            Customer customer = new Customer();
            customer.setUser(user);
            customerRepository.save(customer);
            System.out.println("Customer saved");
        }

        DeviceInfo deviceInfo = user.getDevices().stream()
                .filter(device -> device.getFCMToken().equals(loginRequest.getFCMToken()))
                .findFirst().orElse(null);
        if (deviceInfo != null) {
            deviceInfo.setDeviceType(loginRequest.getDeviceType());
            deviceInfo.setDeviceModel(loginRequest.getDeviceModel());
            deviceInfo.setOperatingSystem(loginRequest.getOperatingSystem());
            deviceInfo.setBrowserName(loginRequest.getBrowerName());
        } else {
            deviceInfo = new DeviceInfo();
            deviceInfo.setFCMToken(loginRequest.getFCMToken());
            deviceInfo.setDeviceType(loginRequest.getDeviceType());
            deviceInfo.setDeviceModel(loginRequest.getDeviceModel());
            deviceInfo.setOperatingSystem(loginRequest.getOperatingSystem());
            deviceInfo.setBrowserName(loginRequest.getBrowerName());

            user.addDevice(deviceInfo);
        }

        userRepository.save(user);
        user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));

        final String jwtToken = jwtUtil.generateToken(user.getEmail());
        UserResponse userResponse = new UserResponse(user.getId(), user.getName(), user.getEmail(),
                user.getPhoneNumber(), user.getRole().name(), user.getStatus().name(), user.getProfileImage(),
                user.getPreferredLanguage());
        AuthenticationResponse authenticationResponse = new AuthenticationResponse(jwtToken, userResponse);
        if (user.getRole() == UserRole.TECHNICIAN) {
            Technician technician = technicianRepository.findByUser(user).orElseThrow(
                    () -> new UserNotFoundException("Technician not found"));
            TechnicianResponse technicianResponse = new TechnicianResponse(technician, user.getPreferredLanguage());
            authenticationResponse.setTechnician(technicianResponse);
        } else if (user.getRole() == UserRole.CUSTOMER) {
            Customer customer = customerRepository.findByUser(user).orElseThrow(
                    () -> new UserNotFoundException("Customer not found"));
            CustomerResponse customerResponse = new CustomerResponse(
                    customer.getId(), customer.getServiceHistory(), customer.getSavedAddresses());
            authenticationResponse.setCustomer(customerResponse);
        }
        return authenticationResponse;
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
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Update the profile image path
        user.setProfileImage(fileName);

        // Save the updated user
        userRepository.save(user);
    }

    public CustomDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return (CustomDetails) authentication.getPrincipal();
        }
        throw new IllegalStateException("User not authenticated");
    }

    public void updatePreferredLanguage(Long userId, EthiopianLanguage preferredLanguage) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        user.setPreferredLanguage(preferredLanguage);
        userRepository.save(user);
    }

    public void initiateChangeEmail(ChangeContactRequest request) {
        // Fetch the user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Handle email change
        if (request.getNewEmail() != null) {
            // Check if the new email already exists in the system
            if (userRepository.existsByEmail(request.getNewEmail())) {
                throw new IllegalStateException("Email already in use");
            }

            user.setPendingEmail(request.getNewEmail());
            String token = UUID.randomUUID().toString();

            // Save the token
            VerificationToken verificationToken = tokenRepository.findByUser(user)
                    .orElse(new VerificationToken(token, LocalDateTime.now().plusHours(1), user));
            verificationToken.setToken(token);
            verificationToken.setExpiryDate(LocalDateTime.now().plusHours(1));
            tokenRepository.save(verificationToken);

            // Send verification email
            emailService.sendVerifyEmailForChange(user, token);
        }

        // Save user
        userRepository.save(user);
    }

    public void initiateChangePhoneNumber(ChangeContactRequest request) {
        // Fetch the user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update phone number immediately if provided
        if (request.getNewPhoneNumber() != null) {
            user.setPhoneNumber(request.getNewPhoneNumber());
        }

        // Save user
        userRepository.save(user);
    }

    public void verifyEmailChange(String token) {
        // Fetch and validate the token
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

        User user = verificationToken.getUser();

        if (user.getPendingEmail() == null) {
            throw new IllegalStateException("No pending email change request.");
        }

        // Update the email
        user.setEmail(user.getPendingEmail());
        user.setPendingEmail(null);

        // Save user and remove the token
        userRepository.save(user);
        tokenRepository.delete(verificationToken);
    }

}
