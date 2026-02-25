package com.home.service.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.home.service.dto.AuthenticationResponse;
import com.home.service.dto.CustomerProfileDTO;
import com.home.service.dto.LoginRequest;
import com.home.service.dto.ProfileUpdateDTO;
import com.home.service.dto.ServiceDTO;
import com.home.service.dto.admin.AddressDTO;
import com.home.service.dto.admin.CustomerDetailDTO;
import com.home.service.config.exceptions.EmailException;
import com.home.service.models.Address;
import com.home.service.models.Booking;
import com.home.service.models.Customer;
import com.home.service.models.SubscriptionPlan;
import com.home.service.models.User;
import com.home.service.models.enums.AccountStatus;
import com.home.service.models.enums.PlanType;
import com.home.service.models.enums.UserRole;
import com.home.service.repositories.AddressRepository;
import com.home.service.repositories.BookingRepository;
import com.home.service.repositories.CustomerRepository;
import com.home.service.repositories.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class CustomerService {

    private final UserService userService;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final BookingRepository bookingRepository;
    private final SubscriptionService subscriptionService;

            public CustomerService(UserService userService, CustomerRepository customerRepository,
                UserRepository userRepository,
            AddressRepository addressRepository, BookingRepository bookingRepository,
            SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
        this.userService = userService;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public AuthenticationResponse signupCustomer(User user) {
        String normalizedEmail = userService.normalizeEmail(user.getEmail());
        if (normalizedEmail != null && userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailException("Email already in use");
        }
        System.out.println("Pass email check");
        user.setStatus(AccountStatus.INACTIVE);

        user.setEmail(normalizedEmail);

        user.setRole(UserRole.CUSTOMER);
        userService.saveUser(user); // Save user with CUSTOMER role
        System.out.println("User saved");
        Customer customer = new Customer();
        customer.setUser(user);
        customerRepository.save(customer);
        System.out.println("Customer saved");

        userService.sendSignupVerification(user);
        System.out.println("Verification sent");

        // Return the same structure as /auth/login
        return userService.buildAuthenticationResponse(user);
    }

    public Customer getCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + id));
    }

    public CustomerProfileDTO getCustomerProfile(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        CustomerProfileDTO dto = new CustomerProfileDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getUser().getName());
        dto.setEmail(customer.getUser().getEmail());
        dto.setPhoneNumber(customer.getUser().getPhoneNumber());
        dto.setServiceHistory(customer.getServiceHistory());
        return dto;
    }

    @Transactional
    public void updateCustomerProfile(Long customerId, ProfileUpdateDTO updateDTO) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        customer.getUser().setName(updateDTO.getName());
        customerRepository.save(customer);
    }

    public AuthenticationResponse loginCustomer(LoginRequest loginRequest) {
        return userService.authenticate(loginRequest);
    }

    @Transactional
    public void deleteCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        customerRepository.delete(customer);
    }

    public Page<CustomerProfileDTO> getAllCustomers(Pageable pageable) {
        Page<Customer> customers = customerRepository.findAll(pageable);
        List<CustomerProfileDTO> customerProfiles = new ArrayList<>();
        for (Customer customer : customers) {
            CustomerProfileDTO dto = new CustomerProfileDTO();
            dto.setId(customer.getId());
            dto.setName(customer.getUser().getName());
            dto.setEmail(customer.getUser().getEmail());
            dto.setPhoneNumber(customer.getUser().getPhoneNumber());
            dto.setServiceHistory(customer.getServiceHistory());
            customerProfiles.add(dto);
        }
        return new PageImpl<>(customerProfiles, pageable, customers.getTotalElements());
    }

    @Transactional
    public Page<CustomerDetailDTO> getFilteredCustomers(String name, Pageable pageable) {
        Specification<Customer> spec = Specification.where(CustomerSpecification.hasName(name));
        return customerRepository.findAll(spec, pageable).map(this::convertToCustomerDetailDTO);
    }

    private CustomerDetailDTO convertToCustomerDetailDTO(Customer customer) {
        List<Booking> bookings = bookingRepository.findByCustomer_Id(customer.getId());
        List<ServiceDTO> services = bookings.stream()
                .map(booking -> new ServiceDTO(booking.getService(), customer.getUser().getPreferredLanguage()))
                .distinct()
                .toList();

        CustomerDetailDTO dto = new CustomerDetailDTO();
        User user = customer.getUser();
        dto.setCustomerId(customer.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setProfileImage(user.getProfileImage());
        dto.setBookings(bookings.size());
        dto.setServices(services);
        List<Address> address = addressRepository.findByCustomerId(customer.getId());
        if (!address.isEmpty()) {
            dto.setAddress(convertToAddressDTO(address.get(0)));
        }

        return dto;
    }

    private AddressDTO convertToAddressDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setSubcity(address.getSubcity());
        dto.setWereda(address.getWereda());
        dto.setCountry(address.getCountry());
        dto.setZipCode(address.getZipCode());
        dto.setLatitude(address.getLatitude());
        dto.setLongitude(address.getLongitude());
        return dto;
    }

    public List<com.home.service.dto.AddressDTO> getCustomerAddresses(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        List<Address> addresses = addressRepository.findByCustomerId(customer.getId());
        List<com.home.service.dto.AddressDTO> addressDTOs = new ArrayList<>();
        for (Address address : addresses) {
            com.home.service.dto.AddressDTO dto = new com.home.service.dto.AddressDTO();
            dto.setId(address.getId());
            dto.setStreet(address.getStreet());
            dto.setCity(address.getCity());
            dto.setSubcity(address.getSubcity());
            dto.setWereda(address.getWereda());
            dto.setCountry(address.getCountry());
            dto.setZipCode(address.getZipCode());
            dto.setLatitude(address.getLatitude());
            dto.setLongitude(address.getLongitude());
            addressDTOs.add(dto);
        }
        return addressDTOs;
    }

    @Transactional
    public void deleteCustomerAddress(Long customerId, Long addressId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Address not found"));
        if (!address.getCustomer().getId().equals(customer.getId())) {
            throw new IllegalArgumentException("Address does not belong to the customer");
        }
        addressRepository.delete(address);
    }

    public Customer createSubscription(Long id, Long planId) {
        Customer customer = getCustomer(id);
        if (customer.getSubscriptionPlan() != null) {
            throw new IllegalStateException("Customer is already subscribed to a tender plan. Use update instead.");
        }
        SubscriptionPlan plan = subscriptionService.getPlanById(planId);
        if (!isTenderPlan(plan.getPlanType())) {
            throw new IllegalArgumentException("Invalid plan type for customer tender");
        }
        customer.setSubscriptionPlan(plan);
        return customerRepository.save(customer);
    }

    public Customer updateSubscription(Long id, Long planId) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        SubscriptionPlan plan = subscriptionService.getPlanById(planId);
        if (!isTenderPlan(plan.getPlanType())) {
            throw new IllegalArgumentException("Invalid plan type for customer tender");
        }
        customer.setSubscriptionPlan(plan);
        return customerRepository.save(customer);
    }

    private boolean isTenderPlan(PlanType planType) {
        return planType == PlanType.TENDER
                || planType == PlanType.CUSTOMER_TENDER;
    }
}
