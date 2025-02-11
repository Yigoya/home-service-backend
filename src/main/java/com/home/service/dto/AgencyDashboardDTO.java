package com.home.service.dto;

import java.util.List;

import com.home.service.models.AgencyBooking;

import com.home.service.models.Services;

public class AgencyDashboardDTO {

    private AgencyProfileDTO agencyProfile;
    private List<ServiceDTO> services;
    private List<AgencyBookingDTO> bookings;
    private Double totalRevenue;
    private Long totalBookings;
    private Long pendingBookings;
    private Long completedBookings;

    public AgencyProfileDTO getAgencyProfile() {
        return agencyProfile;
    }

    public void setAgencyProfile(AgencyProfileDTO agencyProfile) {
        this.agencyProfile = agencyProfile;
    }

    public List<ServiceDTO> getServices() {
        return services;
    }

    public void setServices(List<ServiceDTO> services) {
        this.services = services;
    }

    public List<AgencyBookingDTO> getBookings() {
        return bookings;
    }

    public void setBookings(List<AgencyBookingDTO> bookings) {
        this.bookings = bookings;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(Long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public Long getPendingBookings() {
        return pendingBookings;
    }

    public void setPendingBookings(Long pendingBookings) {
        this.pendingBookings = pendingBookings;
    }

    public Long getCompletedBookings() {
        return completedBookings;
    }

    public void setCompletedBookings(Long completedBookings) {
        this.completedBookings = completedBookings;
    }
}
