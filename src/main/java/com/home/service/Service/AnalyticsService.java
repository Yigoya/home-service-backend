package com.home.service.Service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.home.service.models.Business;
import com.home.service.repositories.BusinessRepository;
import com.home.service.repositories.CustomerRepository;
import com.home.service.repositories.ProductRepository;

import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnalyticsService {

    private final BusinessRepository businessRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public AnalyticsService(BusinessRepository businessRepository, ProductRepository productRepository,
            CustomerRepository customerRepository) {
        this.businessRepository = businessRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;

    }

    public static class OverviewDTO {
        public long views;
        public long leads;
        public long conversions;
        public double revenue;
        public double averageOrderValue;
        public double customerLifetimeValue;
        public double customerAcquisitionCost;
        public double churnRate;

        public OverviewDTO(long views, long leads, long conversions, double revenue, double averageOrderValue,
                double customerLifetimeValue, double customerAcquisitionCost, double churnRate) {
            this.views = views;
            this.leads = leads;
            this.conversions = conversions;
            this.revenue = revenue;
            this.averageOrderValue = averageOrderValue;
            this.customerLifetimeValue = customerLifetimeValue;
            this.customerAcquisitionCost = customerAcquisitionCost;
            this.churnRate = churnRate;
        }
    }

    public static class RevenueDataDTO {
        public String date;
        public double revenue;
        public double target;
        public double expenses;
        public double profit;

        public RevenueDataDTO(String date, double revenue, double target, double expenses, double profit) {
            this.date = date;
            this.revenue = revenue;
            this.target = target;
            this.expenses = expenses;
            this.profit = profit;
        }
    }

    public static class CustomerDataDTO {
        public String date;
        public long customers;

        public CustomerDataDTO(String date, long customers) {
            this.date = date;
            this.customers = customers;
        }
    }

    public OverviewDTO getOverview(Long companyId, String currentUserId) {
        Business company = businessRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));
        if (!company.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        // Dummy data for demonstration
        return new OverviewDTO(1000, 200, 50, 10000.0, 200.0, 500.0, 50.0, 0.05);
    }

    public List<RevenueDataDTO> getRevenue(Long companyId, String period, String start, String end,
            String currentUserId) {
        Business company = businessRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));
        if (!company.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        // Dummy data
        return List.of(new RevenueDataDTO(LocalDateTime.now().toString(), 1000.0, 1200.0, 800.0, 200.0));
    }

    public List<CustomerDataDTO> getCustomersAnalytics(Long companyId, String period, String start, String end,
            String currentUserId) {
        Business company = businessRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));
        if (!company.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        // Dummy data
        return List.of(new CustomerDataDTO(LocalDateTime.now().toString(), 50));
    }
}