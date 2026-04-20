package com.home.service.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.home.service.config.ChapaProperties;
import com.home.service.dto.ChapaCheckoutRequest;
import com.home.service.dto.ChapaCheckoutResponse;
import com.home.service.models.ChapaPayment;
import com.home.service.models.SubscriptionPlan;
import com.home.service.models.User;
import com.home.service.models.enums.ChapaPaymentStatus;
import com.home.service.models.enums.PlanType;
import com.home.service.models.enums.SubscriberType;
import com.home.service.repositories.BusinessRepository;
import com.home.service.repositories.ChapaPaymentRepository;
import com.home.service.repositories.CustomerRepository;
import com.home.service.repositories.TechnicianRepository;

@Service
public class ChapaPaymentService {

    private final ChapaGatewayService chapaGatewayService;
    private final SubscriptionService subscriptionService;
    private final BusinessService businessService;
    private final TechnicianService technicianService;
    private final CustomerService customerService;
    private final ChapaPaymentRepository paymentRepository;
    private final BusinessRepository businessRepository;
    private final TechnicianRepository technicianRepository;
    private final CustomerRepository customerRepository;
    private final ObjectMapper objectMapper;
    private final ChapaProperties chapaProperties;

    public ChapaPaymentService(ChapaGatewayService chapaGatewayService, SubscriptionService subscriptionService,
            BusinessService businessService, TechnicianService technicianService, CustomerService customerService,
            ChapaPaymentRepository paymentRepository,
            BusinessRepository businessRepository, TechnicianRepository technicianRepository,
            CustomerRepository customerRepository, ObjectMapper objectMapper, ChapaProperties chapaProperties) {
        this.chapaGatewayService = chapaGatewayService;
        this.subscriptionService = subscriptionService;
        this.businessService = businessService;
        this.technicianService = technicianService;
        this.customerService = customerService;
        this.paymentRepository = paymentRepository;
        this.businessRepository = businessRepository;
        this.technicianRepository = technicianRepository;
        this.customerRepository = customerRepository;
        this.objectMapper = objectMapper;
        this.chapaProperties = chapaProperties;
    }

    @Transactional
    public ChapaCheckoutResponse createCheckout(ChapaCheckoutRequest request) {
        ContactInfo contactInfo = resolveContactInfo(request);
        if (contactInfo.email() == null || contactInfo.email().isBlank()) {
            throw new IllegalArgumentException("User email is required for Chapa checkout");
        }

        SubscriptionPlan plan = subscriptionService.getPlanById(request.getPlanId());
        validateSubscriberPlan(plan.getPlanType(), request.getSubscriberType());

        BigDecimal amount = plan.getPrice();
        ChapaPayment payment = new ChapaPayment();
        payment.setPlan(plan);
        payment.setPlanType(plan.getPlanType());
        payment.setSubscriberType(request.getSubscriberType());
        payment.setSubscriberId(request.getSubscriberId());
        payment.setAmount(amount);
        payment.setCurrency("ETB");
        payment.setTxRef("SUB" + System.currentTimeMillis());
        payment.setStatus(ChapaPaymentStatus.PENDING);
        paymentRepository.save(payment);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            activateSubscription(payment);
            payment.setStatus(ChapaPaymentStatus.SUCCESS);
            paymentRepository.save(payment);
            return new ChapaCheckoutResponse(null, payment.getTxRef(), "FREE_ACTIVATED");
        }

        ChapaGatewayService.InitializeResult init = chapaGatewayService.initialize(
                payment.getTxRef(),
                amount,
            fallback(contactInfo.firstName(), "Subscription"),
            fallback(contactInfo.lastName(), "User"),
            contactInfo.email(),
                plan.getName());

        payment.setCheckoutUrl(init.checkoutUrl());
        payment.setRawRequest(init.rawRequest());
        payment.setRawResponse(init.rawResponse());
        paymentRepository.save(payment);

        return new ChapaCheckoutResponse(init.checkoutUrl(), payment.getTxRef(), "PENDING");
    }

    @Transactional
    public void handleCallback(Map<String, Object> payload) {
        String txRef = getString(payload, "tx_ref");
        if (txRef == null) {
            txRef = getString(payload, "trx_ref");
        }
        if (txRef == null) {
            throw new IllegalStateException("Chapa callback missing tx_ref");
        }
        final String resolvedTxRef = txRef;

        ChapaPayment payment = paymentRepository.findByTxRef(resolvedTxRef)
            .orElseThrow(() -> new IllegalStateException("Chapa payment not found for tx_ref=" + resolvedTxRef));

        payment.setCallbackReceivedAt(LocalDateTime.now());
        payment.setCallbackPayload(toJson(payload));

        ChapaGatewayService.VerifyResult verifyResult = chapaGatewayService.verify(resolvedTxRef);
        payment.setRawResponse(verifyResult.rawResponse());

        if (isSuccess(verifyResult.txStatus())) {
            if (payment.getStatus() != ChapaPaymentStatus.SUCCESS) {
                payment.setStatus(ChapaPaymentStatus.SUCCESS);
                activateSubscription(payment);
            }
        } else if (isFailed(verifyResult.txStatus())) {
            payment.setStatus(ChapaPaymentStatus.FAILED);
            payment.setErrorMessage("Verification failed: status=" + verifyResult.status() + ", txStatus="
                    + verifyResult.txStatus());
        } else {
            payment.setStatus(ChapaPaymentStatus.PENDING);
            payment.setErrorMessage("Verification pending: status=" + verifyResult.status() + ", txStatus="
                    + verifyResult.txStatus());
        }

        paymentRepository.save(payment);
    }

    @Transactional
    public ChapaCheckoutResponse verifyByTxRef(String txRef) {
        ChapaPayment payment = paymentRepository.findByTxRef(txRef)
                .orElseThrow(() -> new IllegalStateException("Chapa payment not found for tx_ref=" + txRef));

        ChapaGatewayService.VerifyResult verifyResult = chapaGatewayService.verify(txRef);
        payment.setRawResponse(verifyResult.rawResponse());

        if (isSuccess(verifyResult.txStatus())) {
            if (payment.getStatus() != ChapaPaymentStatus.SUCCESS) {
                payment.setStatus(ChapaPaymentStatus.SUCCESS);
                activateSubscription(payment);
            }
        } else if (isFailed(verifyResult.txStatus()) && payment.getStatus() != ChapaPaymentStatus.SUCCESS) {
            payment.setStatus(ChapaPaymentStatus.FAILED);
            payment.setErrorMessage("Verification failed: status=" + verifyResult.status() + ", txStatus="
                    + verifyResult.txStatus());
        } else if (payment.getStatus() != ChapaPaymentStatus.SUCCESS) {
            payment.setStatus(ChapaPaymentStatus.PENDING);
            payment.setErrorMessage("Verification pending: status=" + verifyResult.status() + ", txStatus="
                    + verifyResult.txStatus());
        }

        paymentRepository.save(payment);
        return new ChapaCheckoutResponse(payment.getCheckoutUrl(), payment.getTxRef(), payment.getStatus().name());
    }

    private void activateSubscription(ChapaPayment payment) {
        SubscriberType subscriberType = payment.getSubscriberType();
        Long subscriberId = payment.getSubscriberId();
        SubscriptionPlan plan = payment.getPlan();

        int updatedRows;
        switch (subscriberType) {
            case BUSINESS -> updatedRows = businessRepository.updateSubscriptionPlanById(subscriberId, plan);
            case TECHNICIAN -> updatedRows = technicianRepository.updateSubscriptionPlanById(subscriberId, plan);
            case CUSTOMER -> updatedRows = customerRepository.updateSubscriptionPlanById(subscriberId, plan);
            default -> throw new IllegalArgumentException("Unsupported subscriber type");
        }

        if (updatedRows == 0) {
            throw new IllegalStateException("Failed to activate subscription. Subscriber not found.");
        }
    }

    private void validateSubscriberPlan(PlanType planType, SubscriberType subscriberType) {
        switch (subscriberType) {
            case BUSINESS -> {
                if (!(planType == PlanType.MARKETPLACE || planType == PlanType.YELLOW_PAGES
                        || planType == PlanType.JOBS || planType == PlanType.BUSINESS)) {
                    throw new IllegalArgumentException(
                            "Plan type does not match business subscriber. planType=" + planType
                                    + ", expected one of: MARKETPLACE,YELLOW_PAGES,JOBS,BUSINESS");
                }
            }
            case TECHNICIAN -> {
                if (!(planType == PlanType.HOME_PROFESSIONAL || planType == PlanType.TECHNICIAN)) {
                    throw new IllegalArgumentException(
                            "Plan type does not match technician subscriber. planType=" + planType
                                    + ", expected one of: HOME_PROFESSIONAL,TECHNICIAN");
                }
            }
            case CUSTOMER -> {
                if (!(planType == PlanType.TENDER || planType == PlanType.CUSTOMER_TENDER)) {
                    throw new IllegalArgumentException(
                            "Plan type does not match customer subscriber. planType=" + planType
                                    + ", expected one of: TENDER,CUSTOMER_TENDER");
                }
            }
            default -> throw new IllegalArgumentException("Unknown subscriber type");
        }
    }

    private String fallback(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    private ContactInfo resolveContactInfo(ChapaCheckoutRequest request) {
        if (request.getSubscriberType() == null || request.getSubscriberId() == null) {
            throw new IllegalArgumentException("subscriberType and subscriberId are required");
        }

        User user = switch (request.getSubscriberType()) {
            case BUSINESS -> businessService.getBusiness(request.getSubscriberId()).getOwner();
            case TECHNICIAN -> technicianService.getTechnician(request.getSubscriberId()).getUser();
            case CUSTOMER -> customerService.getCustomer(request.getSubscriberId()).getUser();
            default -> throw new IllegalArgumentException("Unsupported subscriber type");
        };

        if (user == null) {
            throw new IllegalArgumentException("Unable to resolve user for payment");
        }

        String fullName = user.getName();
        String firstName = "Subscription";
        String lastName = "User";
        if (fullName != null && !fullName.isBlank()) {
            String[] parts = fullName.trim().split("\\s+", 2);
            firstName = parts[0];
            if (parts.length > 1) {
                lastName = parts[1];
            }
        }

        String email = user.getEmail();
        if (email == null || email.isBlank()) {
            email = chapaProperties.getDefaultEmail();
        }

        return new ContactInfo(email, firstName, lastName);
    }

    private String getString(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value != null ? value.toString() : null;
    }

    private boolean isSuccess(String txStatus) {
        String normalized = normalize(txStatus);
        return "SUCCESS".equals(normalized)
                || "PAID".equals(normalized)
                || "COMPLETED".equals(normalized);
    }

    private boolean isFailed(String txStatus) {
        String normalized = normalize(txStatus);
        return "FAILED".equals(normalized)
                || "CANCELLED".equals(normalized)
                || "EXPIRED".equals(normalized)
                || "ERROR".equals(normalized);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return null;
        }
    }

    private record ContactInfo(String email, String firstName, String lastName) {
    }
}
