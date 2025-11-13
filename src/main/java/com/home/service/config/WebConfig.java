package com.home.service.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            // @Override
            // public void addCorsMappings(CorsRegistry registry) {
            // registry.addMapping("/**")
            // .allowedOriginPatterns("*") // Use patterns instead of wildcard
            // .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            // .allowedHeaders("*")
            //
            // .allowCredentials(true); // Enable credentials
            // }
            @Override
            public void addCorsMappings(CorsRegistry registry) {

                registry.addMapping("/**");
            }

            @Bean
            public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                // configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173/")); //
                configuration.setAllowedOriginPatterns(Arrays.asList("*")); // Replace with your frontend // origin
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
                configuration.setExposedHeaders(Arrays.asList("Authorization"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
            }

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/uploads/**")
                        .addResourceLocations("file:" +
                                "/opt/uploads/");
            }
        };
    }

}
