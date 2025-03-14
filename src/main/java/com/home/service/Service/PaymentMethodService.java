package com.home.service.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.home.service.models.Address;
import com.home.service.models.PaymentMethod;
import com.home.service.models.User;
import com.home.service.models.enums.PaymentType;
import com.home.service.repositories.AddressRepository;
import com.home.service.repositories.PaymentMethodRepository;
import com.home.service.repositories.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;
import lombok.Setter;

@Service
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public PaymentMethodService(PaymentMethodRepository paymentMethodRepository,
            UserRepository userRepository,
            AddressRepository addressRepository) {
        this.paymentMethodRepository = paymentMethodRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }

    @Getter
    @Setter
    public static class PaymentMethodDTO {
        private String cardNumber;
        private String cardholderName;
        private String expiryDate;
        private Long billingAddressId;
        private PaymentType paymentType;
        private Boolean isDefault;
    }

    @Transactional
    public PaymentMethod createPaymentMethod(PaymentMethodDTO dto, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        PaymentMethod paymentMethod = new PaymentMethod();
        populatePaymentMethod(paymentMethod, dto, user);

        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            // Remove default flag from other payment methods
            paymentMethodRepository.findByUserAndIsDefaultTrue(user)
                    .ifPresent(existing -> existing.setIsDefault(false));
        }

        return paymentMethodRepository.save(paymentMethod);
    }

    @Transactional
    public PaymentMethod updatePaymentMethod(Long id, PaymentMethodDTO dto, Long currentUserId) {
        PaymentMethod paymentMethod = getPaymentMethodById(id, currentUserId);
        populatePaymentMethod(paymentMethod, dto, paymentMethod.getUser());

        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            // Remove default flag from other payment methods
            paymentMethodRepository.findByUserAndIsDefaultTrue(paymentMethod.getUser())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            existing.setIsDefault(false);
                        }
                    });
        }

        return paymentMethodRepository.save(paymentMethod);
    }

    private void populatePaymentMethod(PaymentMethod paymentMethod, PaymentMethodDTO dto, User user) {
        paymentMethod.setUser(user);
        paymentMethod.setCardNumber(dto.getCardNumber());
        paymentMethod.setCardholderName(dto.getCardholderName());
        paymentMethod.setExpiryDate(dto.getExpiryDate());
        paymentMethod.setPaymentType(dto.getPaymentType());
        paymentMethod.setIsDefault(dto.getIsDefault());

        // if (dto.getBillingAddressId() != null) {
        // Address billingAddress =
        // addressRepository.findById(dto.getBillingAddressId())
        // .orElseThrow(() -> new EntityNotFoundException("Billing address not found"));
        // paymentMethod.setBillingAddress(billingAddress);
        // }
    }

    public PaymentMethod getPaymentMethodById(Long id, Long currentUserId) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment method not found"));

        if (!paymentMethod.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }

        return paymentMethod;
    }

    public Page<PaymentMethod> getUserPaymentMethods(Long currentUserId, int page, int size) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        return paymentMethodRepository.findByUser(user, pageable);
    }

    public PaymentMethod getDefaultPaymentMethod(Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return paymentMethodRepository.findByUserAndIsDefaultTrue(user)
                .orElseThrow(() -> new EntityNotFoundException("No default payment method found"));
    }

    @Transactional
    public void deletePaymentMethod(Long id, Long currentUserId) {
        PaymentMethod paymentMethod = getPaymentMethodById(id, currentUserId);
        paymentMethodRepository.delete(paymentMethod);
    }

    @Transactional
    public PaymentMethod setDefaultPaymentMethod(Long id, Long currentUserId) {
        PaymentMethod paymentMethod = getPaymentMethodById(id, currentUserId);
        User user = paymentMethod.getUser();

        // Remove default flag from current default payment method
        paymentMethodRepository.findByUserAndIsDefaultTrue(user)
                .ifPresent(existing -> existing.setIsDefault(false));

        // Set new default payment method
        paymentMethod.setIsDefault(true);
        return paymentMethodRepository.save(paymentMethod);
    }
}