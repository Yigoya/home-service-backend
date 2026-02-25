package com.home.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "telebirr")
@Getter
@Setter
public class TelebirrProperties {
    private String baseUrl;
    private String webBaseUrl;
    private String fabricAppId;
    private String appSecret;
    private String merchantAppId;
    private String merchantCode;
    private String notifyUrl;
    private String redirectUrl;
    private String privateKeyPath;
    private String publicKeyPath;
    private String signType = "SHA256WithRSA";
    private boolean verifyCallbackSignature = true;
}