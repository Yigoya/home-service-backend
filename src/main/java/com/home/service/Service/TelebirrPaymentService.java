package com.home.service.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.home.service.dto.TelebirrCheckoutRequest;
import com.home.service.dto.TelebirrCheckoutResponse;
import com.home.service.models.SubscriptionPlan;
import com.home.service.models.TelebirrPayment;
import com.home.service.models.enums.PlanType;
import com.home.service.models.enums.SubscriberType;
import com.home.service.models.enums.TelebirrPaymentStatus;
import com.home.service.repositories.TelebirrPaymentRepository;
import com.home.service.utils.TelebirrSignatureUtils;
import com.home.service.config.TelebirrProperties;

@Service
public class TelebirrPaymentService {

    private final TelebirrService telebirrService;
    private final SubscriptionService subscriptionService;
    private final BusinessService businessService;
    private final TechnicianService technicianService;
    private final CustomerService customerService;
    private final TelebirrPaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;
    private final TelebirrSignatureUtils signatureUtils;
    private final TelebirrProperties telebirrProperties;

    public TelebirrPaymentService(TelebirrService telebirrService, SubscriptionService subscriptionService,
            BusinessService businessService, TechnicianService technicianService, CustomerService customerService,
            TelebirrPaymentRepository paymentRepository, ObjectMapper objectMapper,
            TelebirrSignatureUtils signatureUtils, TelebirrProperties telebirrProperties) {
        this.telebirrService = telebirrService;
        this.subscriptionService = subscriptionService;
        this.businessService = businessService;
        this.technicianService = technicianService;
        this.customerService = customerService;
        this.paymentRepository = paymentRepository;
        this.objectMapper = objectMapper;
        this.signatureUtils = signatureUtils;
        this.telebirrProperties = telebirrProperties;
    }

    @Transactional
    public TelebirrCheckoutResponse createCheckout(TelebirrCheckoutRequest request) {
        System.out.println("Creating Telebirr checkout for planId: " + request.getPlanId() + ", subscriberType: "
            + request.getSubscriberType() + ", subscriberId: " + request.getSubscriberId());
        SubscriptionPlan plan = subscriptionService.getPlanById(request.getPlanId());
        validateSubscriberPlan(plan.getPlanType(), request.getSubscriberType());

        BigDecimal amount = plan.getPrice();
        TelebirrPayment payment = new TelebirrPayment();
        payment.setPlan(plan);
        payment.setPlanType(plan.getPlanType());
        payment.setSubscriberType(request.getSubscriberType());
        payment.setSubscriberId(request.getSubscriberId());
        payment.setAmount(amount);
        payment.setCurrency("ETB");
        payment.setMerchantOrderId("SUB-" + System.currentTimeMillis());
        payment.setStatus(TelebirrPaymentStatus.PENDING);
        paymentRepository.save(payment);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            activateSubscription(payment);
            payment.setStatus(TelebirrPaymentStatus.SUCCESS);
            paymentRepository.save(payment);
            return new TelebirrCheckoutResponse(null, payment.getMerchantOrderId(), null, "FREE_ACTIVATED");
        }

        String fabricToken = telebirrService.applyFabricToken();
        TelebirrService.PreOrderResult preOrder = telebirrService.createPreOrder(
                fabricToken, plan.getName(), amount, payment.getMerchantOrderId());
        String checkoutUrl = telebirrService.buildCheckoutUrl(preOrder.prepayId());

        payment.setPrepayId(preOrder.prepayId());
        payment.setCheckoutUrl(checkoutUrl);
        payment.setRawRequest(preOrder.rawRequest());
        payment.setRawResponse(preOrder.rawResponse());
        paymentRepository.save(payment);

        return new TelebirrCheckoutResponse(checkoutUrl, payment.getMerchantOrderId(), payment.getPrepayId(),
                "PENDING");
    }

    @Transactional
    public void handleCallback(Map<String, Object> payload) {
        String signature = getString(payload, "sign");
        payload.remove("sign");
        payload.remove("sign_type");

        if (telebirrProperties.isVerifyCallbackSignature()
            && signature != null && telebirrProperties.getPublicKeyPath() != null
                && !telebirrProperties.getPublicKeyPath().isBlank()) {
            boolean verified = signatureUtils.verify(payload, signature, telebirrProperties.getPublicKeyPath());
            if (!verified) {
                throw new IllegalStateException("Telebirr callback signature verification failed");
            }
        }

        String merchantOrderId = getString(payload, "merch_order_id");
        String prepayId = getString(payload, "prepay_id");

        TelebirrPayment payment = null;
        if (merchantOrderId != null) {
            payment = paymentRepository.findByMerchantOrderId(merchantOrderId).orElse(null);
        }
        if (payment == null && prepayId != null) {
            payment = paymentRepository.findByPrepayId(prepayId).orElse(null);
        }
        if (payment == null) {
            throw new IllegalStateException("Telebirr payment not found for callback");
        }

        payment.setCallbackReceivedAt(LocalDateTime.now());
        payment.setCallbackPayload(toJson(payload));

        String tradeStatus = getString(payload, "trade_status");
        if (tradeStatus == null) {
            tradeStatus = getString(payload, "trade_state");
        }

        if (isSuccess(tradeStatus)) {
            if (payment.getStatus() != TelebirrPaymentStatus.SUCCESS) {
                payment.setStatus(TelebirrPaymentStatus.SUCCESS);
                activateSubscription(payment);
            }
        } else {
            payment.setStatus(TelebirrPaymentStatus.FAILED);
            payment.setErrorMessage("Trade status: " + tradeStatus);
        }

        paymentRepository.save(payment);
    }

    private void activateSubscription(TelebirrPayment payment) {
        SubscriberType subscriberType = payment.getSubscriberType();
        Long subscriberId = payment.getSubscriberId();
        Long planId = payment.getPlan().getId();

        switch (subscriberType) {
            case BUSINESS -> {
                var business = businessService.getBusiness(subscriberId);
                if (business.getSubscriptionPlan() == null) {
                    businessService.createSubscription(subscriberId, planId);
                } else {
                    businessService.updateSubscription(subscriberId, planId);
                }
            }
            case TECHNICIAN -> {
                var technician = technicianService.getTechnician(subscriberId);
                if (technician.getSubscriptionPlan() == null) {
                    technicianService.createSubscription(subscriberId, planId);
                } else {
                    technicianService.updateSubscription(subscriberId, planId);
                }
            }
            case CUSTOMER -> {
                var customer = customerService.getCustomer(subscriberId);
                if (customer.getSubscriptionPlan() == null) {
                    customerService.createSubscription(subscriberId, planId);
                } else {
                    customerService.updateSubscription(subscriberId, planId);
                }
            }
            default -> throw new IllegalArgumentException("Unsupported subscriber type");
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

    private String getString(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value != null ? value.toString() : null;
    }

    private boolean isSuccess(String tradeStatus) {
        if (tradeStatus == null) {
            return false;
        }
        String normalized = tradeStatus.trim().toUpperCase();
        return normalized.equals("SUCCESS") || normalized.equals("SUCCESSFUL") || normalized.equals("COMPLETED");
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return null;
        }
    }
}