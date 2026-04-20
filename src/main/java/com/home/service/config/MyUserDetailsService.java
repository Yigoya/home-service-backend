package com.home.service.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.home.service.models.CustomDetails;
import com.home.service.models.User;
import com.home.service.repositories.UserRepository;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public CustomDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(identifier)
                .or(() -> findByPhoneFlexible(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));
        System.out.println("User loadUser: " + user.toString());
        // Return User with authorities if necessary, using hashed password
        return new CustomDetails(user);
    }

    private Optional<User> findByPhoneFlexible(String phone) {
        if (phone == null || phone.isBlank()) {
            return Optional.empty();
        }

        String raw = phone.trim();

        Optional<User> direct = userRepository.findByPhoneNumber(raw);
        if (direct.isPresent()) {
            return direct;
        }

        String digits = raw.startsWith("+") ? raw.substring(1) : raw;
        Optional<User> withoutPlus = userRepository.findByPhoneNumber(digits);
        if (withoutPlus.isPresent()) {
            return withoutPlus;
        }

        if (digits.startsWith("0") && digits.length() == 10) {
            String et = "251" + digits.substring(1);
            Optional<User> etUser = userRepository.findByPhoneNumber(et);
            if (etUser.isPresent()) {
                return etUser;
            }
            return userRepository.findByPhoneNumber("+" + et);
        }

        if (!digits.startsWith("251") && digits.length() == 9) {
            String et = "251" + digits;
            Optional<User> etUser = userRepository.findByPhoneNumber(et);
            if (etUser.isPresent()) {
                return etUser;
            }
            return userRepository.findByPhoneNumber("+" + et);
        }

        return Optional.empty();
    }
}
