package com.home.service.dto;

import java.time.LocalDateTime;

import com.home.service.models.AgencyBooking;
import com.home.service.models.enums.BookingStatus;

public class AgencyBookingDTO {

    private Long id;
    private Long customerId;
    private Long agencyId;
    private Long serviceId;
    private LocalDateTime scheduledDate;
    private String description;
    private BookingStatus status;
    private Double totalCost;

    public AgencyBookingDTO(AgencyBooking agencyBooking) {
        this.id = agencyBooking.getId();
        this.customerId = agencyBooking.getCustomer().getId();
        this.agencyId = agencyBooking.getAgency().getId();
        this.serviceId = agencyBooking.getService().getId();
        this.scheduledDate = agencyBooking.getScheduledDate();
        this.description = agencyBooking.getDescription();
        this.status = agencyBooking.getStatus();
        this.totalCost = agencyBooking.getTotalCost();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(Long agencyId) {
        this.agencyId = agencyId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public LocalDateTime getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDateTime scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }
}