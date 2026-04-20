package com.home.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "fayda")
@Getter
@Setter
public class FaydaProperties {
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String userinfoEndpoint;
    private String clientId;
    private String redirectUri;
    private String scope = "openid profile email";
    private String acrValues = "mosip:idp:acr:generated-code:biometrics";
    private String claimsLocales = "en";
    private String claimsJson;
    private String clientAssertionType = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

    // Can be either base64-encoded JWK JSON/JWK set or plain JSON/JWK set.
    private String privateKeyBase64Jwk;

    // Optional PEM private key path fallback (classpath:/... or absolute path).
    private String privateKeyPath;

    private long authStateTtlSeconds = 600;
    private long verificationTtlSeconds = 900;

    // Toggle whether technician signup must pass Fayda verification.
    private boolean technicianRegistrationVerificationRequired = true;
}
