// package com.home.service.Service;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.BadCredentialsException;
// import
// org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.stereotype.Service;

// import com.home.service.dto.AuthenticationResponse;
// import com.home.service.dto.LoginRequest;
// import com.home.service.config.JwtUtil;
// import com.home.service.config.MyUserDetailsService;

// @Service
// public class AuthenticationService {

// @Autowired
// private AuthenticationManager authenticationManager;

// @Autowired
// private MyUserDetailsService userDetailsService;

// @Autowired
// private JwtUtil jwtUtil;

// public AuthenticationResponse login(LoginRequest loginRequest) {
// try {
// authenticationManager.authenticate(
// new UsernamePasswordAuthenticationToken(
// loginRequest.getEmail(),
// loginRequest.getPassword()));
// } catch (BadCredentialsException e) {
// throw new IllegalArgumentException("Invalid credentials");
// }

// final UserDetails userDetails =
// userDetailsService.loadUserByUsername(loginRequest.getEmail());
// final String jwtToken = jwtUtil.generateToken(userDetails.getUsername());

// return new AuthenticationResponse(jwtToken);
// }
// }
