package com.home.service.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home.service.config.FaydaProperties;
import com.home.service.config.exceptions.BadRequestException;
import com.home.service.dto.fayda.FaydaAuthorizationUrlResponse;
import com.home.service.dto.fayda.FaydaVerifyTechnicianRequest;
import com.home.service.dto.fayda.FaydaVerifyTechnicianResponse;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class FaydaService {

    private static final MediaType FORM = MediaType.parse("application/x-www-form-urlencoded");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final FaydaProperties properties;
    private final ObjectMapper objectMapper;
    private final OkHttpClient client = new OkHttpClient();

    private final ConcurrentHashMap<String, PendingAuthSession> pendingSessionsByState = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, VerifiedIdentitySession> verifiedSessionsByToken = new ConcurrentHashMap<>();

    public FaydaService(FaydaProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public FaydaAuthorizationUrlResponse createAuthorizationUrl() {
        validateBasicConfig();
        pruneExpiredSessions();

        String codeVerifier = randomUrlSafeToken(64);
        String codeChallenge = createCodeChallenge(codeVerifier);
        String state = randomUrlSafeToken(32);

        Instant expiresAt = Instant.now().plus(properties.getAuthStateTtlSeconds(), ChronoUnit.SECONDS);
        pendingSessionsByState.put(state, new PendingAuthSession(codeVerifier, expiresAt));

        String claims = properties.getClaimsJson();
        if (claims == null || claims.isBlank()) {
            claims = "{\"userinfo\":{\"name\":{\"essential\":true},\"phone_number\":{\"essential\":true},\"email\":{\"essential\":false},\"gender\":{\"essential\":false},\"birthdate\":{\"essential\":false},\"individual_id\":{\"essential\":true}},\"id_token\":{}}";
        }

        String authorizationUrl = properties.getAuthorizationEndpoint()
                + "?client_id=" + urlEncode(properties.getClientId())
                + "&response_type=code"
                + "&redirect_uri=" + urlEncode(properties.getRedirectUri())
                + "&scope=" + urlEncode(defaultIfBlank(properties.getScope(), "openid profile email"))
                + "&state=" + urlEncode(state)
                + "&code_challenge=" + urlEncode(codeChallenge)
                + "&code_challenge_method=S256"
                + "&acr_values=" + urlEncode(defaultIfBlank(properties.getAcrValues(), "mosip:idp:acr:generated-code:biometrics"))
                + "&claims_locales=" + urlEncode(defaultIfBlank(properties.getClaimsLocales(), "en"))
                + "&claims=" + urlEncode(claims);

        return new FaydaAuthorizationUrlResponse(authorizationUrl, state, properties.getAuthStateTtlSeconds());
    }

    public FaydaVerifyTechnicianResponse verifyTechnicianAndIssueToken(FaydaVerifyTechnicianRequest request) {
        validateBasicConfig();
        pruneExpiredSessions();

        PendingAuthSession pending = pendingSessionsByState.remove(request.getState());
        if (pending == null || pending.expiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Invalid or expired Fayda state. Please restart verification.");
        }

        String clientAssertion = generateClientAssertionJwt();
        String accessToken = exchangeAuthorizationCode(
                request.getCode(),
                pending.codeVerifier(),
                clientAssertion);

        Map<String, Object> userInfoClaims = fetchAndDecodeUserInfo(accessToken);

        String faydaNationalId = firstNonBlank(userInfoClaims,
                "individual_id", "individualId", "individual_id#en", "individual_id#am");
        if (isBlank(faydaNationalId)) {
            throw new BadRequestException("Fayda response did not include individual_id claim.");
        }

        String providedNationalId = request.getNationalId();
        boolean matched = normalized(providedNationalId).equals(normalized(faydaNationalId));
        if (!matched) {
            throw new BadRequestException("Provided national ID does not match Fayda verification result.");
        }

        String name = firstNonBlank(userInfoClaims, "name", "name#en", "name#am");
        String subject = firstNonBlank(userInfoClaims, "sub");

        String verificationToken = randomUrlSafeToken(48);
        Instant expiresAt = Instant.now().plus(properties.getVerificationTtlSeconds(), ChronoUnit.SECONDS);
        verifiedSessionsByToken.put(
                verificationToken,
                new VerifiedIdentitySession(faydaNationalId, name, subject, expiresAt, false));

        return new FaydaVerifyTechnicianResponse(
                true,
                true,
                faydaNationalId,
                name,
                subject,
                verificationToken);
    }

    public VerifiedIdentity consumeTechnicianVerification(String verificationToken, String expectedNationalId) {
        if (isBlank(verificationToken)) {
            throw new BadRequestException("Fayda verification token is required.");
        }
        if (isBlank(expectedNationalId)) {
            throw new BadRequestException("National ID is required.");
        }

        pruneExpiredSessions();
        VerifiedIdentitySession session = verifiedSessionsByToken.get(verificationToken);
        if (session == null || session.expiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Invalid or expired Fayda verification token.");
        }

        synchronized (session) {
            if (session.used()) {
                throw new BadRequestException("Fayda verification token has already been used.");
            }
            if (!normalized(session.nationalId()).equals(normalized(expectedNationalId))) {
                throw new BadRequestException("National ID mismatch for Fayda verification token.");
            }
            session.setUsed(true);
        }

        verifiedSessionsByToken.remove(verificationToken);
        return new VerifiedIdentity(session.nationalId(), session.name(), session.subject());
    }

    private String exchangeAuthorizationCode(String code, String codeVerifier, String clientAssertion) {
        String form = "grant_type=authorization_code"
                + "&code=" + urlEncode(code)
                + "&redirect_uri=" + urlEncode(properties.getRedirectUri())
                + "&client_id=" + urlEncode(properties.getClientId())
                + "&client_assertion_type=" + urlEncode(defaultIfBlank(properties.getClientAssertionType(),
                        "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"))
                + "&client_assertion=" + urlEncode(clientAssertion)
                + "&code_verifier=" + urlEncode(codeVerifier);

        Request request = new Request.Builder()
                .url(properties.getTokenEndpoint())
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(RequestBody.create(form, FORM))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "{}";
            if (!response.isSuccessful()) {
                throw new BadRequestException("Fayda token exchange failed: " + body);
            }
            Map<String, Object> tokenMap = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {
            });
            Object accessToken = tokenMap.get("access_token");
            if (accessToken == null || accessToken.toString().isBlank()) {
                throw new BadRequestException("Fayda token exchange succeeded but access_token was not returned.");
            }
            return accessToken.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Fayda token exchange failed.", e);
        }
    }

    private Map<String, Object> fetchAndDecodeUserInfo(String accessToken) {
        Request request = new Request.Builder()
                .url(properties.getUserinfoEndpoint())
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new BadRequestException("Fayda userinfo request failed: " + body);
            }
            return decodeUserInfoBody(body);
        } catch (IOException e) {
            throw new IllegalStateException("Fayda userinfo request failed.", e);
        }
    }

    private Map<String, Object> decodeUserInfoBody(String rawBody) throws IOException {
        String body = rawBody == null ? "" : rawBody.trim();
        if (body.startsWith("{")) {
            return objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {
            });
        }

        // If provider returns JWT directly as plain text or JSON string.
        if (body.startsWith("\"") && body.endsWith("\"")) {
            body = objectMapper.readValue(body, String.class);
        }

        String[] parts = body.split("\\.");
        if (parts.length == 3) {
            String payload = parts[1];
            byte[] decoded = Base64.getUrlDecoder().decode(padBase64(payload));
            String payloadJson = new String(decoded, StandardCharsets.UTF_8);
            return objectMapper.readValue(payloadJson, new TypeReference<Map<String, Object>>() {
            });
        }

        throw new BadRequestException("Unsupported Fayda userinfo response format.");
    }

    private String generateClientAssertionJwt() {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiration = Date.from(now.plus(2, ChronoUnit.HOURS));

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setIssuer(properties.getClientId())
                .setSubject(properties.getClientId())
                .setAudience(properties.getTokenEndpoint())
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(loadPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    private PrivateKey loadPrivateKey() {
        try {
            if (!isBlank(properties.getPrivateKeyBase64Jwk())) {
                return loadPrivateKeyFromJwk(properties.getPrivateKeyBase64Jwk());
            }
            if (!isBlank(properties.getPrivateKeyPath())) {
                return loadPrivateKeyFromPem(properties.getPrivateKeyPath());
            }
            throw new IllegalStateException("Fayda private key is not configured.");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load Fayda private key.", e);
        }
    }

    private PrivateKey loadPrivateKeyFromJwk(String base64OrJsonJwk) throws Exception {
        String json;
        String value = base64OrJsonJwk.trim();
        if (value.startsWith("{")) {
            json = value;
        } else {
            json = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
        }

        JsonNode root = objectMapper.readTree(json);
        JsonNode jwkNode = root.has("keys") ? root.path("keys").get(0) : root;

        BigInteger n = base64UrlToBigInteger(requiredClaim(jwkNode, "n"));
        BigInteger e = base64UrlToBigInteger(requiredClaim(jwkNode, "e"));
        BigInteger d = base64UrlToBigInteger(requiredClaim(jwkNode, "d"));
        BigInteger p = base64UrlToBigInteger(requiredClaim(jwkNode, "p"));
        BigInteger q = base64UrlToBigInteger(requiredClaim(jwkNode, "q"));
        BigInteger dp = base64UrlToBigInteger(requiredClaim(jwkNode, "dp"));
        BigInteger dq = base64UrlToBigInteger(requiredClaim(jwkNode, "dq"));
        BigInteger qi = base64UrlToBigInteger(requiredClaim(jwkNode, "qi"));

        RSAPrivateCrtKeySpec keySpec = new RSAPrivateCrtKeySpec(n, e, d, p, q, dp, dq, qi);
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    private PrivateKey loadPrivateKeyFromPem(String keyPath) throws Exception {
        String pem = readKeyString(keyPath);
        String cleaned = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(cleaned);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private String readKeyString(String keyPath) throws IOException {
        if (keyPath.startsWith("classpath:")) {
            String classpathLocation = keyPath.replace("classpath:", "");
            return new String(new ClassPathResource(classpathLocation).getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);
        }
        return Files.readString(Path.of(keyPath));
    }

    private String createCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return BASE64_URL_ENCODER.encodeToString(hashed);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create PKCE code challenge.", e);
        }
    }

    private String randomUrlSafeToken(int bytesLength) {
        byte[] bytes = new byte[bytesLength];
        SECURE_RANDOM.nextBytes(bytes);
        return BASE64_URL_ENCODER.encodeToString(bytes);
    }

    private String firstNonBlank(Map<String, Object> map, String... keys) {
        if (map == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null && !value.toString().isBlank()) {
                return value.toString();
            }
        }
        return null;
    }

    private String normalized(String value) {
        return value == null ? "" : value.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    private String requiredClaim(JsonNode node, String claim) {
        JsonNode value = node.get(claim);
        if (value == null || value.asText().isBlank()) {
            throw new IllegalStateException("Missing required JWK claim: " + claim);
        }
        return value.asText();
    }

    private BigInteger base64UrlToBigInteger(String value) {
        return new BigInteger(1, Base64.getUrlDecoder().decode(padBase64(value)));
    }

    private String padBase64(String value) {
        int mod = value.length() % 4;
        if (mod == 0) {
            return value;
        }
        return value + "=".repeat(4 - mod);
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(defaultIfBlank(value, ""), StandardCharsets.UTF_8);
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void validateBasicConfig() {
        if (isBlank(properties.getAuthorizationEndpoint())
                || isBlank(properties.getTokenEndpoint())
                || isBlank(properties.getUserinfoEndpoint())
                || isBlank(properties.getClientId())
                || isBlank(properties.getRedirectUri())) {
            throw new IllegalStateException(
                    "Fayda configuration is incomplete. Please set authorization/token/userinfo endpoints, client ID, and redirect URI.");
        }
    }

    private void pruneExpiredSessions() {
        Instant now = Instant.now();
        pendingSessionsByState.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(now));
        verifiedSessionsByToken.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(now));
    }

    private record PendingAuthSession(String codeVerifier, Instant expiresAt) {
    }

    public record VerifiedIdentity(String nationalId, String name, String subject) {
    }

    private static final class VerifiedIdentitySession {
        private final String nationalId;
        private final String name;
        private final String subject;
        private final Instant expiresAt;
        private boolean used;

        private VerifiedIdentitySession(String nationalId, String name, String subject, Instant expiresAt, boolean used) {
            this.nationalId = nationalId;
            this.name = name;
            this.subject = subject;
            this.expiresAt = expiresAt;
            this.used = used;
        }

        public String nationalId() {
            return nationalId;
        }

        public String name() {
            return name;
        }

        public String subject() {
            return subject;
        }

        public Instant expiresAt() {
            return expiresAt;
        }

        public boolean used() {
            return used;
        }

        public void setUsed(boolean used) {
            this.used = used;
        }

        @Override
        public int hashCode() {
            return Objects.hash(nationalId, subject, expiresAt, used);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof VerifiedIdentitySession other)) {
                return false;
            }
            return used == other.used
                    && Objects.equals(nationalId, other.nationalId)
                    && Objects.equals(subject, other.subject)
                    && Objects.equals(expiresAt, other.expiresAt);
        }
    }
}
