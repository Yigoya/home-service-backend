package com.home.service.controller;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.home.service.Service.BusinessService;
import com.home.service.Service.CustomerService;
import com.home.service.Service.SubscriptionService;
import com.home.service.Service.TechnicianService;
import com.home.service.models.Business;
import com.home.service.models.CustomDetails;
import com.home.service.models.Customer;
import com.home.service.models.SubscriptionPlan;
import com.home.service.models.Technician;
import com.home.service.models.enums.PlanType;
import com.home.service.models.enums.UserRole;
import com.home.service.repositories.BusinessRepository;
import com.home.service.repositories.CustomerRepository;
import com.home.service.repositories.TechnicianRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final TechnicianService technicianService;
    private final BusinessService businessService;
    private final CustomerService customerService;
    private final TechnicianRepository technicianRepository;
    private final BusinessRepository businessRepository;
    private final CustomerRepository customerRepository;

    public SubscriptionController(SubscriptionService subscriptionService, TechnicianService technicianService,
            BusinessService businessService, CustomerService customerService,
            TechnicianRepository technicianRepository, BusinessRepository businessRepository,
            CustomerRepository customerRepository) {
        this.subscriptionService = subscriptionService;
        this.technicianService = technicianService;
        this.businessService = businessService;
        this.customerService = customerService;
        this.technicianRepository = technicianRepository;
        this.businessRepository = businessRepository;
        this.customerRepository = customerRepository;
    }

    // Get all plans by type with language support
    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlanResponse>> getPlansByType(
            @RequestParam PlanType planType,
            @RequestParam(defaultValue = "ENGLISH") String language) {
        List<SubscriptionPlan> plans = subscriptionService.getPlansByType(planType);
        List<SubscriptionPlanResponse> response = plans.stream().map(plan -> {
            List<String> features = switch (language.toUpperCase()) {
                case "AMHARIC" -> plan.getFeaturesAmharic();
                case "OROMO" -> plan.getFeaturesOromo();
                default -> plan.getFeaturesEnglish();
            };
            return new SubscriptionPlanResponse(plan.getId(), plan.getName(), plan.getPrice(), plan.getDurationMonths(),
                    features);
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // Get subscription for Technician
    @GetMapping("/technician/{id}")
    public ResponseEntity<SubscriptionPlan> getTechnicianSubscription(@PathVariable Long id) {
        Technician technician = technicianService.getTechnician(id);
        return ResponseEntity.ok(technician.getSubscriptionPlan());
    }

    @PostMapping("/technician/{id}")
    public ResponseEntity<String> createTechnicianSubscription(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionPlanRequest request,
            @AuthenticationPrincipal CustomDetails currentUser) {
        enforceTechnicianSubscriptionAccess(currentUser, id);
        Technician subscribedTechnician = technicianService.createSubscription(id, request.getPlanId());
        return ResponseEntity.status(201)
                .body("Technician subscription created successfully with ID: " + subscribedTechnician.getId());
    }

    // Update subscription for Technician
    @CrossOrigin(originPatterns = "*")
    @PutMapping("/technician/{id}")
    public ResponseEntity<String> updateTechnicianSubscription(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionPlanRequest request,
            @AuthenticationPrincipal CustomDetails currentUser) {
        enforceTechnicianSubscriptionAccess(currentUser, id);
        Technician updatedTechnician = technicianService.updateSubscription(id, request.getPlanId());
        return ResponseEntity.ok("Technician subscription updated successfully with ID: " + updatedTechnician.getId());
    }

    // Get subscription for Business
    @GetMapping("/business/{id}")
    public ResponseEntity<SubscriptionPlan> getBusinessSubscription(@PathVariable Long id) {
        Business business = businessService.getBusiness(id);
        return ResponseEntity.ok(business.getSubscriptionPlan());
    }

    @PostMapping("/business/{id}")
    public ResponseEntity<String> createBusinessSubscription(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionPlanRequest request,
            @AuthenticationPrincipal CustomDetails currentUser) {
        enforceBusinessSubscriptionAccess(currentUser, id);
        Business subscribedBusiness = businessService.createSubscription(id, request.getPlanId());
        return ResponseEntity.status(201)
                .body("Business subscription created successfully with ID: " + subscribedBusiness.getId());
    }

    // Update subscription for Business
    @CrossOrigin(originPatterns = "*")
    @PutMapping("/business/{id}")
    public ResponseEntity<String> updateBusinessSubscription(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionPlanRequest request,
            @AuthenticationPrincipal CustomDetails currentUser) {
        enforceBusinessSubscriptionAccess(currentUser, id);
        Business updatedBusiness = businessService.updateSubscription(id, request.getPlanId());
        return ResponseEntity.ok("Business subscription updated successfully with ID: " + updatedBusiness.getId());
    }

    // Get subscription for Customer
    @GetMapping("/customer/{id}")
    public ResponseEntity<SubscriptionPlan> getCustomerSubscription(@PathVariable Long id) {
        Customer customer = customerService.getCustomer(id);
        return ResponseEntity.ok(customer.getSubscriptionPlan());
    }

    @PostMapping("/customer/{id}")
    public ResponseEntity<String> createCustomerSubscription(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionPlanRequest request,
            @AuthenticationPrincipal CustomDetails currentUser) {
        enforceCustomerSubscriptionAccess(currentUser, id);
        Customer subscribedCustomer = customerService.createSubscription(id, request.getPlanId());
        return ResponseEntity.status(201)
                .body("Customer subscription created successfully with ID: " + subscribedCustomer.getId());
    }

    // Update subscription for Customer
    @CrossOrigin(originPatterns = "*")
    @PutMapping("/customer/{id}")
    public ResponseEntity<String> updateCustomerSubscription(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionPlanRequest request,
            @AuthenticationPrincipal CustomDetails currentUser) {
        enforceCustomerSubscriptionAccess(currentUser, id);
        Customer updatedCustomer = customerService.updateSubscription(id, request.getPlanId());
        return ResponseEntity.ok("Customer subscription updated successfully with ID: " + updatedCustomer.getId());
    }

    private void enforceTechnicianSubscriptionAccess(CustomDetails currentUser, Long technicianId) {
        if (isPrivileged(currentUser)) {
            return;
        }

        if (currentUser == null || currentUser.getId() == null || currentUser.getRole() != UserRole.TECHNICIAN) {
            throw new AccessDeniedException("You are not authorized to modify this technician subscription");
        }

        Technician technician = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Technician not found"));

        if (technician.getUser() == null || !currentUser.getId().equals(technician.getUser().getId())) {
            throw new AccessDeniedException("You are not authorized to modify this technician subscription");
        }
    }

    private void enforceBusinessSubscriptionAccess(CustomDetails currentUser, Long businessId) {
        if (isPrivileged(currentUser)) {
            return;
        }

        if (currentUser == null || currentUser.getId() == null || currentUser.getRole() != UserRole.BUSINESS) {
            throw new AccessDeniedException("You are not authorized to modify this business subscription");
        }

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Business not found"));

        if (business.getOwner() == null || !currentUser.getId().equals(business.getOwner().getId())) {
            throw new AccessDeniedException("You are not authorized to modify this business subscription");
        }
    }

    private void enforceCustomerSubscriptionAccess(CustomDetails currentUser, Long customerId) {
        if (isPrivileged(currentUser)) {
            return;
        }

        if (currentUser == null || currentUser.getId() == null || currentUser.getRole() != UserRole.CUSTOMER) {
            throw new AccessDeniedException("You are not authorized to modify this customer subscription");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Customer not found"));

        if (customer.getUser() == null || !currentUser.getId().equals(customer.getUser().getId())) {
            throw new AccessDeniedException("You are not authorized to modify this customer subscription");
        }
    }

    private boolean isPrivileged(CustomDetails currentUser) {
        if (currentUser == null || currentUser.getRole() == null) {
            return false;
        }
        return currentUser.getRole() == UserRole.ADMIN || currentUser.getRole() == UserRole.OPERATOR;
    }
}

class SubscriptionPlanRequest {
    @NotNull(message = "planId is required")
    private Long planId;

    @JsonIgnore
    private final Map<String, Object> unexpectedFields = new HashMap<>();

    @JsonAnySetter
    public void captureUnexpectedField(String key, Object value) {
        if (!"planId".equals(key)) {
            unexpectedFields.put(key, value);
        }
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    @jakarta.validation.constraints.AssertTrue(message = "Only planId is allowed in request body")
    public boolean hasNoUnexpectedFields() {
        return unexpectedFields.isEmpty();
    }
}

class SubscriptionPlanResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer durationMonths;
    private List<String> features;

    public SubscriptionPlanResponse(Long id, String name, BigDecimal price, Integer durationMonths,
            List<String> features) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.durationMonths = durationMonths;
        this.features = features;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getDurationMonths() {
        return durationMonths;
    }

    public List<String> getFeatures() {
        return features;
    }
}