package com.home.service.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.home.service.models.enums.*;
import lombok.*;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone number is required")
    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Email(message = "Invalid email format")
    @Column(nullable = true)
    private String email;

    @Email(message = "Invalid email format")
    private String pendingEmail;

    @NotBlank(message = "Password is required")
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private String profileImage;

    @Enumerated(EnumType.STRING)
    private AccountStatus status = AccountStatus.INACTIVE;

    @Enumerated(EnumType.STRING)
    private EthiopianLanguage preferredLanguage = EthiopianLanguage.ENGLISH;

    private String authProvider;
    private String providerId;
    private Integer failedLoginAttempts = 0;
    private LocalDateTime lockoutUntil;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeviceInfo> devices = new ArrayList<>();

    public void addDevice(DeviceInfo deviceInfo) {
        devices.add(deviceInfo);
        deviceInfo.setUser(this);
    }

    public void removeDevice(DeviceInfo deviceInfo) {
        devices.remove(deviceInfo);
        deviceInfo.setUser(null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPendingEmail() {
        return pendingEmail;
    }

    public void setPendingEmail(String pendingEmail) {
        this.pendingEmail = pendingEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public EthiopianLanguage getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(EthiopianLanguage preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getLockoutUntil() {
        return lockoutUntil;
    }

    public void setLockoutUntil(LocalDateTime lockoutUntil) {
        this.lockoutUntil = lockoutUntil;
    }

    public List<DeviceInfo> getDevices() {
        return devices;
    }

    public void setDevices(List<DeviceInfo> devices) {
        this.devices = devices;
    }
}
