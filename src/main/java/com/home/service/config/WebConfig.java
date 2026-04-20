package com.home.service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

@Configuration
public class WebConfig {

    private static final Set<String> ALLOWED_PUBLIC_UPLOAD_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp");

    @Bean
    public WebMvcConfigurer corsConfigurer(ObjectOwnershipInterceptor objectOwnershipInterceptor) {
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
                registry.addMapping("/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization")
                        .allowCredentials(true);
            }

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/uploads/**")
                        .addResourceLocations("file:/opt/uploads/")
                        .resourceChain(true)
                        .addResolver(new PathResourceResolver() {
                            @Override
                            protected Resource getResource(String resourcePath, Resource location) throws IOException {
                                if (isBlockedUploadResource(resourcePath)) {
                                    return null;
                                }
                                return super.getResource(resourcePath, location);
                            }

                            private boolean isBlockedUploadResource(String resourcePath) {
                                int dotIndex = resourcePath.lastIndexOf('.');
                                if (dotIndex < 0 || dotIndex == resourcePath.length() - 1) {
                                    return true;
                                }

                                String ext = resourcePath.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
                                return !ALLOWED_PUBLIC_UPLOAD_EXTENSIONS.contains(ext);
                            }
                        });
            }

            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(objectOwnershipInterceptor)
                        .addPathPatterns("/**")
                        .excludePathPatterns(
                                "/auth/**",
                                "/uploads/**",
                                "/static/**",
                                "/public/**",
                                "/error");
            }
        };
    }

}
