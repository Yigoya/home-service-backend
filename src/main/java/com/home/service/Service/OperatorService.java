package com.home.service.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.home.service.dto.AuthenticationResponse;
import com.home.service.dto.LoginRequest;
import com.home.service.dto.OperatorProfileDTO;
import com.home.service.dto.OperatorSignupRequest;
import com.home.service.config.exceptions.EmailException;
import com.home.service.models.Operator;
import com.home.service.models.User;
import com.home.service.models.enums.UserRole;
import com.home.service.repositories.OperatorRepository;
import com.home.service.repositories.UserRepository;
import com.home.service.services.FileStorageService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class OperatorService {

    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final OperatorRepository operatorRepository;
    private final UserRepository userRepository;

    public OperatorService(UserService userService, FileStorageService fileStorageService,
            OperatorRepository operatorRepository, UserRepository userRepository) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
        this.operatorRepository = operatorRepository;
        this.userRepository = userRepository;
    }

    public AuthenticationResponse signupOperator(OperatorSignupRequest signupRequest) {
        String normalizedEmail = userService.normalizeEmail(signupRequest.getEmail());
        if (normalizedEmail != null && userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailException("Email already in use");
        }
        // Create and save User
        User user = new User();
        user.setName(signupRequest.getName());
        user.setEmail(normalizedEmail);
        user.setPhoneNumber(signupRequest.getPhoneNumber());
        user.setPassword(signupRequest.getPassword());
        user.setRole(UserRole.OPERATOR);

        userService.saveUser(user);
        userService.sendPhoneVerificationCode(user);

        // Create and save Operator
        Operator operator = new Operator();
        operator.setUser(user);
        operator.setAssignedRegion(signupRequest.getAssignedRegion());

        // Store ID card image
        String idCardImagePath = fileStorageService.storeFile(signupRequest.getIdCardImage());
        operator.setIdCardImage(idCardImagePath);

        operatorRepository.save(operator);

        // Return the same payload as /auth/login
        return userService.buildAuthenticationResponse(user);
    }

    public AuthenticationResponse loginOperator(LoginRequest loginRequest) {
        return userService.authenticate(loginRequest);
    }

    public Page<OperatorProfileDTO> getAllOperators(Pageable pageable) {
        Page<Operator> operators = operatorRepository.findAll(pageable);
        List<OperatorProfileDTO> operatorProfiles = operators.stream()
                .map(operator -> new OperatorProfileDTO(operator))
                .collect(Collectors.toList());
        return new PageImpl<>(operatorProfiles, pageable, operators.getTotalElements());
    }

    public void deleteOperator(Long id) {
        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Operator not found"));
        operatorRepository.delete(operator);
    }
}
