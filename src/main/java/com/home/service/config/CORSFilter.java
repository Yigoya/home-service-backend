// package com.home.service.config;

// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.ServletRequest;
// import jakarta.servlet.ServletResponse;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.GenericFilterBean;

// import java.io.IOException;
// import java.util.Arrays;
// import java.util.List;

// @Component
// public class CORSFilter extends GenericFilterBean {
// private final List<String> allowedOrigins =
// Arrays.asList("http://localhost:5173", "https://localhost:4200");

// @Override
// public void doFilter(ServletRequest servletRequest, ServletResponse
// servletResponse, FilterChain filterChain)
// throws IOException, ServletException {
// HttpServletRequest request = (HttpServletRequest) servletRequest;
// HttpServletResponse response = (HttpServletResponse) servletResponse;

// String origin = request.getHeader("Origin");
// // response.setHeader("Access-Control-Allow-Origin",
// // allowedOrigins.contains(origin) ? origin : "");

// response.setHeader("Access-Control-Allow-Origin",
// request.getHeader("Origin"));
// // response.setHeader("Access-Control-Allow-Origin",
// "http://localhost:4200");
// // response.setHeader("Access-Control-Allow-Origin",
// // "http://196.188.127.241:4200");

// // response.setHeader("Vary", "Origin");

// response.setHeader("Access-Control-Allow-Credentials", "true");

// response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE,
// OPTIONS");
// response.setHeader("Access-Control-Max-Age", "3600");
// response.setHeader("Access-Control-Allow-Headers",
// "Origin, authorization, content-type, xsrf-token, Sec-Fetch-Mode,
// Sec-Fetch-Site, Sec-Fetch-Dest");
// response.addHeader("Access-Control-Expose-Headers", "xsrf-token");
// if ("OPTIONS".equals(request.getMethod())) {
// response.setStatus(HttpServletResponse.SC_OK);
// } else {
// filterChain.doFilter(request, response);
// }
// }
// }
