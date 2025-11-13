package com.home.service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.home.service.Service.BusinessService;
import com.home.service.Service.CustomerService;
import com.home.service.Service.SubscriptionService;
import com.home.service.Service.TechnicianService;
import com.home.service.models.Business;
import com.home.service.models.Customer;
import com.home.service.models.SubscriptionPlan;
import com.home.service.models.Technician;
import com.home.service.models.enums.PlanType;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final TechnicianService technicianService;
    private final BusinessService businessService;
    private final CustomerService customerService;

    public SubscriptionController(SubscriptionService subscriptionService, TechnicianService technicianService,
            BusinessService businessService, CustomerService customerService) {
        this.subscriptionService = subscriptionService;
        this.technicianService = technicianService;
        this.businessService = businessService;
        this.customerService = customerService;
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
            @PathVariable Long id, @RequestBody SubscriptionPlanRequest request) {
        Technician subscribedTechnician = technicianService.createSubscription(id, request.getPlanId());
        return ResponseEntity.status(201)
                .body("Technician subscription created successfully with ID: " + subscribedTechnician.getId());
    }

    // Update subscription for Technician
    @CrossOrigin(originPatterns = "*")
    @PutMapping("/technician/{id}")
    public ResponseEntity<String> updateTechnicianSubscription(
            @PathVariable Long id, @RequestBody SubscriptionPlanRequest request) {
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
            @PathVariable Long id, @RequestBody SubscriptionPlanRequest request) {
        Business subscribedBusiness = businessService.createSubscription(id, request.getPlanId());
        return ResponseEntity.status(201)
                .body("Business subscription created successfully with ID: " + subscribedBusiness.getId());
    }

    // Update subscription for Business
    @CrossOrigin(originPatterns = "*")
    @PutMapping("/business/{id}")
    public ResponseEntity<String> updateBusinessSubscription(
            @PathVariable Long id, @RequestBody SubscriptionPlanRequest request) {
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
            @PathVariable Long id, @RequestBody SubscriptionPlanRequest request) {
        Customer subscribedCustomer = customerService.createSubscription(id, request.getPlanId());
        return ResponseEntity.status(201)
                .body("Customer subscription created successfully with ID: " + subscribedCustomer.getId());
    }

    // Update subscription for Customer
    @CrossOrigin(originPatterns = "*")
    @PutMapping("/customer/{id}")
    public ResponseEntity<String> updateCustomerSubscription(
            @PathVariable Long id, @RequestBody SubscriptionPlanRequest request) {
        Customer updatedCustomer = customerService.updateSubscription(id, request.getPlanId());
        return ResponseEntity.ok("Customer subscription updated successfully with ID: " + updatedCustomer.getId());
    }
}

class SubscriptionPlanRequest {
    private Long planId;

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
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