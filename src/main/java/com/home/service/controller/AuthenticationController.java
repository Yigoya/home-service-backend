package com.home.service.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.ListUsersPage;
import com.home.service.Service.AdminService;
import com.home.service.Service.CustomerService;
import com.home.service.Service.OperatorService;
import com.home.service.Service.PaymentProofService;
import com.home.service.Service.TechnicianService;
import com.home.service.Service.UserService;
import com.home.service.dto.AdminLoginRequest;
import com.home.service.dto.AuthenticationResponse;
import com.home.service.dto.CustomerResponse;
import com.home.service.dto.LoginRequest;
import com.home.service.dto.NewPasswordRequest;
import com.home.service.dto.OperatorSignupRequest;
import com.home.service.dto.SocialLoginRequest;
import com.home.service.dto.TechnicianResponse;
import com.home.service.dto.TechnicianSignupRequest;
import com.home.service.dto.UploadPaymentProofRequest;
import com.home.service.dto.UserResponse;
import com.home.service.config.JwtUtil;
import com.home.service.config.exceptions.UserNotFoundException;
import com.home.service.models.CustomDetails;
import com.home.service.models.Customer;
import com.home.service.models.DeviceInfo;
import com.home.service.models.Technician;
import com.home.service.models.TechnicianProofResponse;
import com.home.service.models.User;
import com.home.service.models.enums.UserRole;
import com.home.service.repositories.DeviceInfoRepository;
import com.home.service.repositories.UserRepository;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthenticationController {

    private final CustomerService customerService;
    private final TechnicianService technicianService;
    private final OperatorService operatorService;
    private final AdminService adminService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PaymentProofService paymentProofService;
    private final UserRepository userRepository;
    private final DeviceInfoRepository deviceInfoRepository;

    public AuthenticationController(CustomerService customerService, TechnicianService technicianService,
            OperatorService operatorService, AdminService adminService, UserService userService, JwtUtil jwtUtil,
            PaymentProofService paymentProofService, UserRepository userRepository,
            DeviceInfoRepository deviceInfoRepository) {
        this.customerService = customerService;
        this.technicianService = technicianService;
        this.operatorService = operatorService;
        this.adminService = adminService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.paymentProofService = paymentProofService;
        this.userRepository = userRepository;
        this.deviceInfoRepository = deviceInfoRepository;
    }

    @PostMapping("/login")
    public AuthenticationResponse loginCustomer(@Valid @RequestBody LoginRequest loginRequest) {
        System.out.println("token token token " + loginRequest.getDeviceModel());
        return userService.authenticate(loginRequest);
    }

    @PostMapping("/social-login")
    public ResponseEntity<?> handleSocialLogin(@Valid @RequestBody SocialLoginRequest loginRequest) {
        try {
            AuthenticationResponse authenticationResponse = userService.handleSocialLogin(loginRequest);
            return ResponseEntity.ok(authenticationResponse);
        } catch (FirebaseAuthException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }

    public FirebaseToken verifyToken(String idToken) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        return decodedToken;
    }

    @GetMapping("/test")
    public ResponseEntity<String> testFirebaseConnection() {
        try {
            FirebaseApp firebaseApp = FirebaseApp.getInstance();
            return ResponseEntity.ok("Firebase App Name: " + firebaseApp.getName());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Firebase is not initialized: " + e.getMessage());
        }
    }

    @GetMapping("/list-users")
    public ResponseEntity<?> listUsers() {
        try {
            ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);
            List<Map<String, String>> users = new ArrayList<>();
            for (ExportedUserRecord user : page.iterateAll()) {
                users.add(Map.of(
                        "uid", user.getUid(),
                        "email", user.getEmail()));
            }
            return ResponseEntity.ok(users);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error listing users: " + e.getMessage());
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token) {
        return ResponseEntity.ok(userService.verifyToken(token));
    }

    @PostMapping("/password-reset-request")
    public ResponseEntity<String> requestPasswordReset(@RequestParam("email") String email) {
        return ResponseEntity.ok(userService.requestPasswordReset(email));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody NewPasswordRequest newPasswordRequest) {
        return ResponseEntity.ok(userService.resetPassword(newPasswordRequest));
    }

    @PostMapping("/customer/signup")
    public String signupCustomer(@Valid @RequestBody User user) {
        return customerService.signupCustomer(user);
    }

    @PostMapping("/technician/signup")
    public String signupTechnician(@Valid @ModelAttribute TechnicianSignupRequest signupRequest) {
        return technicianService.signupTechnician(signupRequest);
    }

    @PostMapping("/operator/signup")
    public String signupOperator(@Valid @ModelAttribute OperatorSignupRequest signupRequest) {
        return operatorService.signupOperator(signupRequest);
    }

    @PostMapping("/admin/login")
    public ResponseEntity<AuthenticationResponse> loginAdmin(@Valid @RequestBody AdminLoginRequest loginRequest) {
        if (adminService.authenticateAdmin(loginRequest.getUsername(), loginRequest.getPassword())) {
            final String jwtToken = jwtUtil.generateToken("admin");
            final UserResponse user = new UserResponse();
            user.setName("admin");
            user.setRole("ADMIN");
            return ResponseEntity.ok(new AuthenticationResponse(jwtToken, user));
        } else {
            throw new BadCredentialsException("Invalid admin credentials.");
        }
    }

    @PostMapping("/suspend/{userId}")
    public ResponseEntity<String> suspendUser(@PathVariable Long userId) {
        String message = userService.suspendUser(userId);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/delete/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        String message = userService.deleteUser(userId);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/upload-proof")
    public ResponseEntity<String> uploadPaymentProof(@ModelAttribute UploadPaymentProofRequest request) {
        String responseMessage = paymentProofService.uploadPaymentProof(request.getFile(), request.getTechnicianId());
        return ResponseEntity.ok(responseMessage);
    }

    @PutMapping("/review-proof/{technicianId}")
    public ResponseEntity<String> reviewPaymentProof(
            @PathVariable Long technicianId,
            @RequestParam("approve") boolean approve) {

        String responseMessage = paymentProofService.reviewPaymentProof(technicianId, approve);
        if (responseMessage.equals("No pending documents found for review.")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseMessage);
        }
        return ResponseEntity.ok(responseMessage);
    }

    @DeleteMapping("/logout")
    public ResponseEntity<String> deleteDevice(@RequestParam String firebaseToken,
            @AuthenticationPrincipal CustomDetails currentUser) {
        // Find the logged-in user
        User user = userRepository.findByEmail(currentUser.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find the device with the given firebaseToken
        DeviceInfo device = deviceInfoRepository.findByFCMToken(firebaseToken)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        // Ensure the device belongs to the logged-in user
        if (!device.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to delete this device");
        }

        // Delete the device
        deviceInfoRepository.delete(device);
        return ResponseEntity.ok("Device successfully deleted");
    }
}
