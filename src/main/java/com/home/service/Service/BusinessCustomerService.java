
package com.home.service.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.home.service.models.Business;
import com.home.service.models.BusinessCustomer;
import com.home.service.models.enums.CompanySize;
import com.home.service.models.enums.CustomerStatus;
import com.home.service.repositories.BusinessCustomerRepository;
import com.home.service.repositories.BusinessRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class BusinessCustomerService {

    private final BusinessCustomerRepository customerRepository;
    private final BusinessRepository businessRepository;

    public BusinessCustomerService(BusinessCustomerRepository customerRepository,
            BusinessRepository businessRepository) {
        this.customerRepository = customerRepository;
        this.businessRepository = businessRepository;
    }

    public static class CustomerDTO {
        public String name;
        public String email;
        public String contactPerson;
        public String phone;
        public String industry;
        public CompanySize size;
        public CustomerStatus status;
        public String lastOrderDate;
        public double totalSpent;
    }

    public Page<BusinessCustomer> getCustomers(Long companyId, int page, int size, String sort, String search,
            String currentUserId) {
        Business company = businessRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));
        if (!company.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        Sort sorting = Sort.by(Sort.Direction.fromString(sort.split(",")[1]), sort.split(",")[0]);
        Pageable pageable = PageRequest.of(page, size, sorting);
        return customerRepository.findByCompanyId(companyId, search, pageable);
    }

    public BusinessCustomer createCustomer(Long companyId, CustomerDTO dto, String currentUserId) {
        Business company = businessRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));
        if (!company.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        BusinessCustomer customer = new BusinessCustomer();
        populateCustomer(customer, dto, company);
        return customerRepository.save(customer);
    }

    public BusinessCustomer updateCustomer(Long companyId, Long customerId, CustomerDTO dto, String currentUserId) {
        BusinessCustomer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        if (!customer.getCompany().getId().equals(companyId)
                || !customer.getCompany().getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        populateCustomer(customer, dto, customer.getCompany());
        return customerRepository.save(customer);
    }

    public void deleteCustomer(String companyId, Long customerId, String currentUserId) {
        BusinessCustomer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        if (!customer.getCompany().getId().equals(companyId)
                || !customer.getCompany().getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        customerRepository.delete(customer);
    }

    private void populateCustomer(BusinessCustomer customer, CustomerDTO dto, Business company) {
        customer.setName(dto.name);
        customer.setEmail(dto.email);
        customer.setContactPerson(dto.contactPerson);
        customer.setPhone(dto.phone);
        customer.setIndustry(dto.industry);
        customer.setSize(dto.size);
        customer.setStatus(dto.status);
        customer.setLastOrderDate(dto.lastOrderDate);
        customer.setTotalSpent(dto.totalSpent);
        customer.setCompany(company);
    }
}