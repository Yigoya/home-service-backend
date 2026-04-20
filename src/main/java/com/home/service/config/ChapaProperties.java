package com.home.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "chapa")
@Getter
@Setter
public class ChapaProperties {
    private String secretKey;
    private String callbackUrl;
    private String returnUrl;
    private String currency = "ETB";
    private String defaultEmail;
    private String customizationTitle;
    private String customizationDescription;
    private String customizationLogo;
}
