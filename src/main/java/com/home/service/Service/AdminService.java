package com.home.service.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    public boolean authenticateAdmin(String username, String password) {
        return adminUsername.equals(username) && adminPassword.equals(password);
    }
}
