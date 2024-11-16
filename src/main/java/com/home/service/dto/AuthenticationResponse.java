package com.home.service.dto;

public class AuthenticationResponse {
    private final String token;
    private final UserResponse user;
    private TechnicianResponse technician;
    private CustomerResponse customer;
    private OperatorResponse operator;

    public AuthenticationResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;

    }

    public String getToken() {
        return token;
    }

    public UserResponse getUser() {
        return user;
    }

    public TechnicianResponse getTechnician() {
        return technician;
    }

    public void setTechnician(TechnicianResponse technician) {
        this.technician = technician;
    }

    public CustomerResponse getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerResponse customer) {
        this.customer = customer;
    }

    public OperatorResponse getOperator() {
        return operator;
    }

    public void setOperator(OperatorResponse operator) {
        this.operator = operator;
    }

}
