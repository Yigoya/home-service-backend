package com.home.service.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;
    private final AuthRateLimitFilter authRateLimitFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter, AuthRateLimitFilter authRateLimitFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.authRateLimitFilter = authRateLimitFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .requireCsrfProtectionMatcher(request -> {
                    // Keep CSRF for cookie-auth browser requests, skip for Bearer token API clients (Postman/mobile)
                    String method = request.getMethod();
                    if (HttpMethod.GET.matches(method)
                            || HttpMethod.HEAD.matches(method)
                            || HttpMethod.OPTIONS.matches(method)
                            || HttpMethod.TRACE.matches(method)) {
                        return false;
                    }

                    String authHeader = request.getHeader("Authorization");
                    return authHeader == null || !authHeader.startsWith("Bearer ");
                })
                .ignoringRequestMatchers(
                    "/auth/login",
                    "/auth/social-login",
                    "/auth/register",
                    "/auth/customer/signup",
                    "/auth/technician/signup",
                    "/auth/operator/signup",
                    "/auth/admin/login",
                    "/auth/password-reset-request",
                    "/auth/reset-password",
                    "/auth/resend-verification",
                    "/payment/telebirr/notify"))
                .authorizeHttpRequests(auth -> auth
                // CORS preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Public authentication/account endpoints
                .requestMatchers(HttpMethod.POST,
                    "/auth/login",
                    "/auth/social-login",
                    "/auth/resend-verification",
                    "/auth/password-reset-request",
                    "/auth/reset-password",
                    "/auth/register",
                    "/auth/customer/signup",
                    "/auth/technician/signup",
                    "/auth/operator/signup",
                    "/auth/admin/login")
                .permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/auth/token-login",
                    "/auth/verify",
                    "/auth/csrf")
                .permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()

                // Public read-only app content endpoints
                .requestMatchers(HttpMethod.GET,
                    "/home",
                    "/admin/services",
                    "/services",
                    "/services/**",
                    "/service-categories",
                    "/service-categories/**",
                    "/technicians",
                    "/technicians/**",
                    "/search/**",
                    "/review/technician/**",
                    "/districts",
                    "/app/version",
                    "/jobs",
                    "/jobs/**",
                    "/profiles/company/search",
                    "/profiles/company/detail/**",
                    "/marketplace/businesses",
                    "/marketplace/businesses/*",
                    "/marketplace/businesses/*/products",
                    "/marketplace/products/**",
                    "/marketplace/services/**",
                    "/promotions/public/**")
                .permitAll()

                // Public webhook/callback endpoint
                .requestMatchers(HttpMethod.POST, "/payment/telebirr/notify").permitAll()

                // Public static/websocket assets
                .requestMatchers(
                    "/uploads/**",
                    "/static/**",
                    "/public/**",
                    "/ws/**",
                    "/error")
                .permitAll()

                        .anyRequest().authenticated())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(
                (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Unauthorized")));

        http.addFilterBefore(authRateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
