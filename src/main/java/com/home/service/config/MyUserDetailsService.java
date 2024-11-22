package com.home.service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.home.service.config.exceptions.UserNotFoundException;
import com.home.service.models.CustomDetails;
import com.home.service.models.User;
import com.home.service.repositories.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public CustomDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        System.out.println("User loadUser: " + user.toString());
        // Return User with authorities if necessary, using hashed password
        return new CustomDetails(user);
    }
}
