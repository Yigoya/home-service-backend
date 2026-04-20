package com.home.service.config;

import com.home.service.models.AgencyProfile;
import com.home.service.models.Booking;
import com.home.service.models.CustomDetails;
import com.home.service.models.Notification;
import com.home.service.models.enums.UserRole;
import com.home.service.repositories.AgencyProfileRepository;
import com.home.service.repositories.BookingRepository;
import com.home.service.repositories.BusinessRepository;
import com.home.service.repositories.CustomerRepository;
import com.home.service.repositories.NotificationRepository;
import com.home.service.repositories.TechnicianRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class ObjectOwnershipInterceptor implements HandlerInterceptor {

    private final CustomerRepository customerRepository;
    private final TechnicianRepository technicianRepository;
    private final BusinessRepository businessRepository;
    private final AgencyProfileRepository agencyProfileRepository;
    private final BookingRepository bookingRepository;
    private final NotificationRepository notificationRepository;

    public ObjectOwnershipInterceptor(CustomerRepository customerRepository,
                                      TechnicianRepository technicianRepository,
                                      BusinessRepository businessRepository,
                                      AgencyProfileRepository agencyProfileRepository,
                                      BookingRepository bookingRepository,
                                      NotificationRepository notificationRepository) {
        this.customerRepository = customerRepository;
        this.technicianRepository = technicianRepository;
        this.businessRepository = businessRepository;
        this.agencyProfileRepository = agencyProfileRepository;
        this.bookingRepository = bookingRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return true;
        }

        if (!(authentication.getPrincipal() instanceof CustomDetails currentUser)) {
            return true;
        }

        if (isPrivileged(currentUser)) {
            return true;
        }

        @SuppressWarnings("unchecked")
        Map<String, String> pathVariables =
                (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        String uri = request.getRequestURI();
        String method = request.getMethod();

        if (pathVariables != null) {
            validatePathVariableOwnership(pathVariables, uri, method, currentUser);
        }

        validateQueryParameterOwnership(request, uri, method, currentUser);

        return true;
    }

    private void validatePathVariableOwnership(Map<String, String> vars, String uri, String method, CustomDetails currentUser) {
        if (vars.containsKey("customerId")) {
            assertCustomerOwned(parseLong(vars.get("customerId")), currentUser.getId());
        }

        if (vars.containsKey("technicianId")) {
            assertTechnicianOwned(parseLong(vars.get("technicianId")), currentUser.getId());
        }

        if (vars.containsKey("businessId") && isBusinessSensitiveEndpoint(uri, method)) {
            assertBusinessOwned(parseLong(vars.get("businessId")), currentUser.getId());
        }

        if (vars.containsKey("agencyId") && isAgencySensitiveEndpoint(uri, method)) {
            assertAgencyOwned(parseLong(vars.get("agencyId")), currentUser.getId());
        }

        if (vars.containsKey("ownerId")) {
            assertSameUser(parseLong(vars.get("ownerId")), currentUser.getId());
        }

        if (vars.containsKey("userId") && isUserSensitiveEndpoint(uri, method)) {
            assertSameUser(parseLong(vars.get("userId")), currentUser.getId());
        }

        if (vars.containsKey("bookingId") && isBookingSensitiveEndpoint(uri, method)) {
            assertBookingParticipant(parseLong(vars.get("bookingId")), currentUser.getId());
        }

        if (vars.containsKey("id")) {
            Long id = parseLong(vars.get("id"));
            if (uri.startsWith("/profile/technician/") || uri.startsWith("/subscriptions/technician/")) {
                assertTechnicianOwned(id, currentUser.getId());
            } else if (uri.startsWith("/profile/customer/") || uri.startsWith("/subscriptions/customer/")) {
                assertCustomerOwned(id, currentUser.getId());
            } else if (uri.startsWith("/subscriptions/business/")) {
                assertBusinessOwned(id, currentUser.getId());
            } else if (uri.startsWith("/profiles/seeker/")) {
                assertSameUser(id, currentUser.getId());
            } else if (uri.startsWith("/booking/") && isBookingSensitiveEndpoint(uri, method)) {
                assertBookingParticipant(id, currentUser.getId());
            } else if (uri.startsWith("/notifications/") && uri.endsWith("/mark-as-read")) {
                assertNotificationOwned(id, currentUser.getId());
            }
        }
    }

    private void validateQueryParameterOwnership(HttpServletRequest request, String uri, String method, CustomDetails currentUser) {
        String userIdParam = request.getParameter("userId");
        if (userIdParam != null && isUserSensitiveEndpoint(uri, method)) {
            assertSameUser(parseLong(userIdParam), currentUser.getId());
        }

        String customerIdParam = request.getParameter("customerId");
        if (customerIdParam != null) {
            assertCustomerOwned(parseLong(customerIdParam), currentUser.getId());
        }

        String recipientIdParam = request.getParameter("recipientId");
        if (recipientIdParam != null && uri.startsWith("/notifications/")) {
            assertSameUser(parseLong(recipientIdParam), currentUser.getId());
        }
    }

    private boolean isBusinessSensitiveEndpoint(String uri, String method) {
        if (uri.startsWith("/subscriptions/business/")) {
            return true;
        }
        if (uri.startsWith("/business/") && !"GET".equalsIgnoreCase(method)) {
            return true;
        }
        return uri.startsWith("/promotions/promotions/business/");
    }

    private boolean isAgencySensitiveEndpoint(String uri, String method) {
        if (uri.startsWith("/agency/")) {
            return true;
        }
        return uri.startsWith("/tender-agency/") && !"GET".equalsIgnoreCase(method);
    }

    private boolean isUserSensitiveEndpoint(String uri, String method) {
        if (uri.startsWith("/auth/")) {
            return false;
        }
        if (uri.startsWith("/profiles/company/") && "GET".equalsIgnoreCase(method)) {
            return false;
        }
        return true;
    }

    private boolean isBookingSensitiveEndpoint(String uri, String method) {
        if (uri.startsWith("/booking/customer/") || uri.startsWith("/booking/technician/")) {
            return true;
        }
        return uri.startsWith("/booking/") && !"GET".equalsIgnoreCase(method);
    }

    private void assertSameUser(Long targetUserId, Long authenticatedUserId) {
        if (targetUserId == null || authenticatedUserId == null || !targetUserId.equals(authenticatedUserId)) {
            deny();
        }
    }

    private void assertCustomerOwned(Long customerId, Long authenticatedUserId) {
        Long ownerUserId = customerRepository.findById(customerId)
                .map(c -> c.getUser() != null ? c.getUser().getId() : null)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        if (ownerUserId == null || !ownerUserId.equals(authenticatedUserId)) {
            deny();
        }
    }

    private void assertTechnicianOwned(Long technicianId, Long authenticatedUserId) {
        Long ownerUserId = technicianRepository.findById(technicianId)
                .map(t -> t.getUser() != null ? t.getUser().getId() : null)
                .orElseThrow(() -> new EntityNotFoundException("Technician not found"));

        if (ownerUserId == null || !ownerUserId.equals(authenticatedUserId)) {
            deny();
        }
    }

    private void assertBusinessOwned(Long businessId, Long authenticatedUserId) {
        Long ownerUserId = businessRepository.findById(businessId)
                .map(b -> b.getOwner() != null ? b.getOwner().getId() : null)
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));

        if (ownerUserId == null || !ownerUserId.equals(authenticatedUserId)) {
            deny();
        }
    }

    private void assertAgencyOwned(Long agencyId, Long authenticatedUserId) {
        AgencyProfile agency = agencyProfileRepository.findById(agencyId)
                .orElseThrow(() -> new EntityNotFoundException("Agency not found"));

        Long ownerUserId = agency.getUser() != null ? agency.getUser().getId() : null;
        if (ownerUserId == null || !ownerUserId.equals(authenticatedUserId)) {
            deny();
        }
    }

    private void assertBookingParticipant(Long bookingId, Long authenticatedUserId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        Long customerUserId = booking.getCustomer() != null && booking.getCustomer().getUser() != null
                ? booking.getCustomer().getUser().getId()
                : null;
        Long technicianUserId = booking.getTechnician() != null && booking.getTechnician().getUser() != null
                ? booking.getTechnician().getUser().getId()
                : null;

        if (!authenticatedUserId.equals(customerUserId) && !authenticatedUserId.equals(technicianUserId)) {
            deny();
        }
    }

    private void assertNotificationOwned(Long notificationId, Long authenticatedUserId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        Long recipientUserId = notification.getRecipient() != null ? notification.getRecipient().getId() : null;
        if (recipientUserId == null || !recipientUserId.equals(authenticatedUserId)) {
            deny();
        }
    }

    private boolean isPrivileged(CustomDetails currentUser) {
        return currentUser.getRole() == UserRole.ADMIN || currentUser.getRole() == UserRole.OPERATOR;
    }

    private Long parseLong(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            throw new AccessDeniedException("Invalid identifier.");
        }
    }

    private void deny() {
        throw new AccessDeniedException("You are not authorized to access this resource.");
    }
}
