package com.home.service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import com.home.service.Service.AdminService;
import com.home.service.Service.CustomerService;
import com.home.service.Service.OperatorService;
import com.home.service.Service.PaymentProofService;
import com.home.service.Service.TechnicianService;
import com.home.service.Service.UserService;
import com.home.service.dto.AdminLoginRequest;
import com.home.service.dto.AuthenticationResponse;
import com.home.service.dto.LoginRequest;
import com.home.service.dto.NewPasswordRequest;
import com.home.service.dto.OperatorSignupRequest;
import com.home.service.dto.TechnicianSignupRequest;
import com.home.service.dto.UploadPaymentProofRequest;
import com.home.service.dto.UserResponse;
import com.home.service.config.JwtUtil;
import com.home.service.models.TechnicianProofResponse;
import com.home.service.models.User;

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

    public AuthenticationController(CustomerService customerService, TechnicianService technicianService,
            OperatorService operatorService, AdminService adminService, UserService userService, JwtUtil jwtUtil,
            PaymentProofService paymentProofService) {
        this.customerService = customerService;
        this.technicianService = technicianService;
        this.operatorService = operatorService;
        this.adminService = adminService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.paymentProofService = paymentProofService;
    }

    @PostMapping("/login")
    public AuthenticationResponse loginCustomer(@Valid @RequestBody LoginRequest loginRequest) {
        return userService.authenticate(loginRequest);
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

    @GetMapping("/pending-proofs")
    public ResponseEntity<List<TechnicianProofResponse>> getTechniciansWithPendingProofs() {
        List<TechnicianProofResponse> response = paymentProofService.getTechniciansWithPendingProofs();
        return ResponseEntity.ok(response);
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
}
