package com.home.service.Service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.home.service.models.Business;
import com.home.service.models.Supplier;
import com.home.service.models.enums.SupplierStatus;
import com.home.service.repositories.BusinessRepository;
import com.home.service.repositories.SupplierRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final BusinessRepository businessRepository;

    public SupplierService(SupplierRepository supplierRepository, BusinessRepository businessRepository) {
        this.supplierRepository = supplierRepository;
        this.businessRepository = businessRepository;
    }

    public static class SupplierDTO {
        public String name;
        public String contactPerson;
        public String email;
        public String phone;
        public String address;
        public String city;
        public String state;
        public String zip;
        public String country;
        public String category;
        public SupplierStatus status;
        public double rating;
        public String paymentTerms;
        public int leadTime;
        public double minimumOrderValue;
        public List<String> products;
    }

    public Page<Supplier> getSuppliers(Long companyId, int page, int size, String currentUserId) {
        Business company = businessRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));
        if (!company.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        Pageable pageable = PageRequest.of(page, size);
        return supplierRepository.findByCompanyId(companyId, pageable);
    }

    public Supplier createSupplier(Long companyId, SupplierDTO dto, String currentUserId) {
        Business company = businessRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));
        if (!company.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        Supplier supplier = new Supplier();
        populateSupplier(supplier, dto, company);
        return supplierRepository.save(supplier);
    }

    public Supplier updateSupplier(Long companyId, Long supplierId, SupplierDTO dto, Long currentUserId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));
        if (!supplier.getCompany().getId().equals(companyId)
                || !supplier.getCompany().getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        populateSupplier(supplier, dto, supplier.getCompany());
        return supplierRepository.save(supplier);
    }

    public void deleteSupplier(Long companyId, Long supplierId, Long currentUserId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));
        if (!supplier.getCompany().getId().equals(companyId)
                || !supplier.getCompany().getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        supplierRepository.delete(supplier);
    }

    private void populateSupplier(Supplier supplier, SupplierDTO dto, Business company) {
        supplier.setName(dto.name);
        supplier.setContactPerson(dto.contactPerson);
        supplier.setEmail(dto.email);
        supplier.setPhone(dto.phone);
        supplier.setAddress(dto.address);
        supplier.setCity(dto.city);
        supplier.setState(dto.state);
        supplier.setZip(dto.zip);
        supplier.setCountry(dto.country);
        supplier.setCategory(dto.category);
        supplier.setStatus(dto.status);
        supplier.setRating(dto.rating);
        supplier.setPaymentTerms(dto.paymentTerms);
        supplier.setLeadTime(dto.leadTime);
        supplier.setMinimumOrderValue(dto.minimumOrderValue);
        supplier.setProducts(dto.products);
        supplier.setCompany(company);
    }
}