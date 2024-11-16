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
import com.home.service.dto.UserResponse;
import com.home.service.dto.admin.AddressDTO;
import com.home.service.dto.admin.CustomerDetailDTO;
import com.home.service.config.JwtUtil;
import com.home.service.config.exceptions.EmailException;
import com.home.service.models.Address;
import com.home.service.models.Booking;
import com.home.service.models.Customer;
import com.home.service.models.CustomerAddress;
import com.home.service.models.Services;
import com.home.service.models.User;
import com.home.service.models.enums.AccountStatus;
import com.home.service.models.enums.UserRole;
import com.home.service.repositories.AddressRepository;
import com.home.service.repositories.BookingRepository;
import com.home.service.repositories.CustomerRepository;
import com.home.service.repositories.UserRepository;
import com.home.service.services.EmailService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class CustomerService {

    private final UserService userService;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final AddressRepository addressRepository;
    private final BookingRepository bookingRepository;

    public CustomerService(UserService userService, CustomerRepository customerRepository,
            UserRepository userRepository, EmailService emailService, JwtUtil jwtUtil,
            AddressRepository addressRepository, BookingRepository bookingRepository) {
        this.userService = userService;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
        this.addressRepository = addressRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public String signupCustomer(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailException("Email already in use");
        }
        System.out.println("Pass email check");
        user.setStatus(AccountStatus.INACTIVE);

        user.setRole(UserRole.CUSTOMER);
        userService.saveUser(user); // Save user with CUSTOMER role
        System.out.println("User saved");
        Customer customer = new Customer();
        customer.setUser(user);
        customerRepository.save(customer);
        System.out.println("Customer saved");

        try {
            emailService.sendVerifyEmail(user);
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmailException("Email not sent but account created");
        }
        System.out.println("Email sent");

        return "Account created successfully";
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

    public Page<CustomerDetailDTO> getFilteredCustomers(String name, Pageable pageable) {
        Specification<Customer> spec = Specification.where(CustomerSpecification.hasName(name));
        return customerRepository.findAll(spec, pageable).map(this::convertToCustomerDetailDTO);
    }

    private CustomerDetailDTO convertToCustomerDetailDTO(Customer customer) {
        List<Booking> bookings = bookingRepository.findByCustomer_Id(customer.getId());
        List<ServiceDTO> services = bookings.stream()
                .map(booking -> new ServiceDTO(booking.getService()))
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

}
