package com.home.service.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
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
import com.home.service.Service.TenderAgencyService;
import com.home.service.Service.UserService;
import com.home.service.dto.AdminLoginRequest;
import com.home.service.dto.AuthenticationResponse;
import com.home.service.dto.LoginRequest;
import com.home.service.dto.NewPasswordRequest;
import com.home.service.dto.OperatorSignupRequest;
import com.home.service.dto.PasswordResetRequest;
import com.home.service.dto.SocialLoginRequest;
import com.home.service.dto.TechnicianSignupRequest;
import com.home.service.dto.UploadPaymentProofRequest;
import com.home.service.dto.UserRegistrationRequest;
import com.home.service.dto.UserResponse;
import com.home.service.config.JwtUtil;
import com.home.service.models.CustomDetails;
import com.home.service.models.DeviceInfo;
import com.home.service.models.User;
import com.home.service.repositories.DeviceInfoRepository;
import com.home.service.repositories.UserRepository;
import com.home.service.services.AuthCookieService;
import com.home.service.services.RefreshTokenService;
import com.home.service.services.SmsService;

import jakarta.validation.Valid;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final SmsService smsService;
    private final RefreshTokenService refreshTokenService;
    private final AuthCookieService authCookieService;
    // private final TenderAgencyService tenderAgencyService;

    public AuthenticationController(CustomerService customerService, TechnicianService technicianService,
            OperatorService operatorService, AdminService adminService, UserService userService, JwtUtil jwtUtil,
            PaymentProofService paymentProofService, UserRepository userRepository,
            DeviceInfoRepository deviceInfoRepository, SmsService smsService,
            RefreshTokenService refreshTokenService, AuthCookieService authCookieService,
            TenderAgencyService tenderAgencyService) {
        this.customerService = customerService;
        this.technicianService = technicianService;
        this.operatorService = operatorService;
        this.adminService = adminService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.paymentProofService = paymentProofService;
        this.userRepository = userRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.smsService = smsService;
        this.refreshTokenService = refreshTokenService;
        this.authCookieService = authCookieService;
        // this.tenderAgencyService = tenderAgencyService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> loginCustomer(@Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {
        System.out.println("token token token " + loginRequest.getDeviceModel());
        AuthenticationResponse auth = userService.authenticate(loginRequest);
        User user = userRepository.findById(auth.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String refreshToken = refreshTokenService.issueToken(user);
        authCookieService.writeAuthCookies(response, auth.getToken(), refreshToken);
        return ResponseEntity.ok(auth);
    }

    @PostMapping("/social-login")
    public ResponseEntity<?> handleSocialLogin(@Valid @RequestBody SocialLoginRequest loginRequest,
            HttpServletResponse response) {
        try {
            AuthenticationResponse authenticationResponse = userService.handleSocialLogin(loginRequest);
            User user = userRepository.findById(authenticationResponse.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            String refreshToken = refreshTokenService.issueToken(user);
            authCookieService.writeAuthCookies(response, authenticationResponse.getToken(), refreshToken);
            return ResponseEntity.ok(authenticationResponse);
        } catch (FirebaseAuthException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }

    @GetMapping("/token-login")
    public ResponseEntity<?> loginWithToken(@RequestParam("token") String token, HttpServletResponse response) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body("Token is required");
        }

        try {
            AuthenticationResponse authenticationResponse = userService.authenticateWithToken(token);
            User user = userRepository.findById(authenticationResponse.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            String refreshToken = refreshTokenService.issueToken(user);
            authCookieService.writeAuthCookies(response, authenticationResponse.getToken(), refreshToken);
            return ResponseEntity.ok(authenticationResponse);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping("/csrf")
    public ResponseEntity<Map<String, String>> csrf(CsrfToken token) {
        return ResponseEntity.ok(Map.of("token", token.getToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, AuthCookieService.REFRESH_TOKEN_COOKIE);
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing refresh token");
        }

        try {
            RefreshTokenService.RotationResult rotated = refreshTokenService.rotate(refreshToken);
            AuthenticationResponse auth = userService.buildAuthenticationResponse(rotated.user());
            authCookieService.writeAuthCookies(response, auth.getToken(), rotated.newPlainToken());
            return ResponseEntity.ok(auth);
        } catch (IllegalStateException ex) {
            authCookieService.clearAuthCookies(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
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
    public ResponseEntity<AuthenticationResponse> verifyAccount(
            @RequestParam(value = "token", required = false) String token,
            @RequestParam(value = "code", required = false) String code,
            HttpServletResponse response) {
        AuthenticationResponse auth = userService.verifyTokenOrCode(token, code);
        User user = userRepository.findById(auth.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String refreshToken = refreshTokenService.issueToken(user);
        authCookieService.writeAuthCookies(response, auth.getToken(), refreshToken);
        return ResponseEntity.ok(auth);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber) {
        userService.resendVerification(email, phoneNumber);
        return ResponseEntity.ok("Verification sent");
    }

    @PostMapping("/password-reset-request")
    public ResponseEntity<String> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        return ResponseEntity.ok(userService.requestPasswordReset(request.getEmail(), request.getPhoneNumber()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody NewPasswordRequest newPasswordRequest) {
        return ResponseEntity.ok(userService.resetPassword(newPasswordRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request,
            HttpServletResponse servletResponse) {
        User registeredUser = userService.registerUser(request);
        // Return same shape as /auth/login: JWT + user/role-specific payload
        AuthenticationResponse auth = userService.buildAuthenticationResponse(registeredUser);
        String refreshToken = refreshTokenService.issueToken(registeredUser);
        authCookieService.writeAuthCookies(servletResponse, auth.getToken(), refreshToken);
        return ResponseEntity.ok(auth);
    }

    @PostMapping("/customer/signup")
    public ResponseEntity<AuthenticationResponse> signupCustomer(@Valid @RequestBody User user,
            HttpServletResponse response) {
        AuthenticationResponse auth = customerService.signupCustomer(user);
        User saved = userRepository.findById(auth.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String refreshToken = refreshTokenService.issueToken(saved);
        authCookieService.writeAuthCookies(response, auth.getToken(), refreshToken);
        return ResponseEntity.ok(auth);
    }

    @PostMapping("/technician/signup")
    public ResponseEntity<AuthenticationResponse> signupTechnician(@Valid @ModelAttribute TechnicianSignupRequest signupRequest,
            HttpServletResponse response) {
        AuthenticationResponse auth = technicianService.signupTechnician(signupRequest);
        User saved = userRepository.findById(auth.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String refreshToken = refreshTokenService.issueToken(saved);
        authCookieService.writeAuthCookies(response, auth.getToken(), refreshToken);
        return ResponseEntity.ok(auth);
    }

    @PostMapping("/operator/signup")
    public ResponseEntity<AuthenticationResponse> signupOperator(@Valid @ModelAttribute OperatorSignupRequest signupRequest,
            HttpServletResponse response) {
        AuthenticationResponse auth = operatorService.signupOperator(signupRequest);
        User saved = userRepository.findById(auth.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String refreshToken = refreshTokenService.issueToken(saved);
        authCookieService.writeAuthCookies(response, auth.getToken(), refreshToken);
        return ResponseEntity.ok(auth);
    }

    @PostMapping("/admin/login")
    public ResponseEntity<AuthenticationResponse> loginAdmin(@Valid @RequestBody AdminLoginRequest loginRequest,
            HttpServletResponse response) {
        if (adminService.authenticateAdmin(loginRequest.getUsername(), loginRequest.getPassword())) {
            final String jwtToken = jwtUtil.generateToken("admin");
            final UserResponse user = new UserResponse();
            user.setName("admin");
            user.setRole("ADMIN");
            authCookieService.writeAuthCookies(response, jwtToken, null);
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

    @CrossOrigin(originPatterns = "*")
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

    @CrossOrigin(originPatterns = "*")
@DeleteMapping("/logout")
    public ResponseEntity<String> deleteDevice(@RequestParam(required = false) String firebaseToken,
            @AuthenticationPrincipal CustomDetails currentUser,
            HttpServletRequest request,
            HttpServletResponse response) {
        String refreshToken = extractCookie(request, AuthCookieService.REFRESH_TOKEN_COOKIE);
        refreshTokenService.revoke(refreshToken);
        authCookieService.clearAuthCookies(response);

        // Find the logged-in user
        User user = userRepository.findByEmail(currentUser.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (firebaseToken == null || firebaseToken.isBlank()) {
            return ResponseEntity.ok("Logged out");
        }

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

    @PostMapping("/send-sms")
    public ResponseEntity<String> sendSms(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        String message = request.get("message");
        String response = smsService.sendSms(to, message);
        return ResponseEntity.ok(response);
    }

    private String extractCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
