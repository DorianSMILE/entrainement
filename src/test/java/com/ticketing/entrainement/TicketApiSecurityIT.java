package com.ticketing.entrainement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TicketApiSecurityIT {

    private static final String USER = "admin";
    private static final String PASS = "admin";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("ticket")
            .withUsername("postgres")
            .withPassword("password");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);

        r.add("spring.flyway.enabled", () -> "true");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");

        r.add("security.jwt.access-secret", () -> "c3VwZXItc2VjcmV0LXRlc3QtYWNjZXNzLXNlY3JldC0xMjM0NTY3ODkw");
        r.add("security.jwt.refresh-secret", () -> "c3VwZXItc2VjcmV0LXRlc3QtcmVmcmVzaC1zZWNyZXQtMTIzNDU2Nzg5MA==");
        r.add("security.jwt.access-expiration-minutes", () -> "15");
        r.add("security.jwt.refresh-expiration-days", () -> "7");
    }

    @Autowired TestRestTemplate rest;
    @Autowired ObjectMapper om;

    private JsonNode login() throws Exception {
        Map<String, Object> body = Map.of(
                "username", USER,
                "password", PASS
        );

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> res = rest.exchange(
                "/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(body, h),
                String.class
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isNotBlank();

        JsonNode json = om.readTree(res.getBody());
        assertThat(json.hasNonNull("accessToken")).isTrue();
        assertThat(json.hasNonNull("refreshToken")).isTrue();
        assertThat(json.hasNonNull("tokenType")).isTrue();
        assertThat(json.get("tokenType").asText()).isEqualTo("Bearer");
        assertThat(json.hasNonNull("expiresInMinutes")).isTrue();
        assertThat(json.get("expiresInMinutes").asLong()).isGreaterThan(0);

        return json;
    }

    private ResponseEntity<String> refresh(String refreshToken) {
        Map<String, Object> body = Map.of("refreshToken", refreshToken);

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);

        return rest.exchange(
                "/auth/refresh",
                HttpMethod.POST,
                new HttpEntity<>(body, h),
                String.class
        );
    }

    private ResponseEntity<String> createTicketWithToken(String token) {
        Map<String, Object> body = Map.of(
                "title", "Ticket JWT",
                "description", "Created with token",
                "priority", "MEDIUM"
        );

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.setBearerAuth(token);

        return rest.exchange(
                "/tickets",
                HttpMethod.POST,
                new HttpEntity<>(body, h),
                String.class
        );
    }

    private ResponseEntity<String> createTicketWithoutToken() {
        Map<String, Object> body = Map.of(
                "title", "Security check",
                "description", "Should require JWT",
                "priority", "MEDIUM"
        );

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);

        return rest.exchange(
                "/tickets",
                HttpMethod.POST,
                new HttpEntity<>(body, h),
                String.class
        );
    }

    @Test
    void login_returns_200_and_access_refresh_tokens() throws Exception {
        JsonNode json = login();

        assertThat(json.get("accessToken").asText()).isNotBlank();
        assertThat(json.get("refreshToken").asText()).isNotBlank();
    }

    @Test
    void refresh_with_valid_refresh_returns_200_and_new_tokens() throws Exception {
        JsonNode loginJson = login();

        String access1 = loginJson.get("accessToken").asText();
        String refresh1 = loginJson.get("refreshToken").asText();

        ResponseEntity<String> refreshRes = refresh(refresh1);
        assertThat(refreshRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refreshRes.getBody()).isNotBlank();

        JsonNode refreshJson = om.readTree(refreshRes.getBody());
        String access2 = refreshJson.get("accessToken").asText();
        String refresh2 = refreshJson.get("refreshToken").asText();

        assertThat(access2).isNotBlank();
        assertThat(refresh2).isNotBlank();
        assertThat(refreshJson.get("tokenType").asText()).isEqualTo("Bearer");
        assertThat(refreshJson.get("expiresInMinutes").asLong()).isGreaterThan(0);

        // rotation active dans ton implémentation actuelle
        assertThat(access2).isNotEqualTo(access1);
        assertThat(refresh2).isNotEqualTo(refresh1);
    }

    @Test
    void refresh_with_invalid_token_returns_401() {
        ResponseEntity<String> res = refresh("not-a-valid-refresh-token");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void create_ticket_without_token_returns_401() {
        ResponseEntity<String> res = createTicketWithoutToken();
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void create_ticket_with_valid_access_token_returns_201() throws Exception {
        JsonNode loginJson = login();
        String accessToken = loginJson.get("accessToken").asText();

        ResponseEntity<String> res = createTicketWithToken(accessToken);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getBody()).isNotBlank();

        JsonNode created = om.readTree(res.getBody());
        assertThat(created.hasNonNull("id")).isTrue();
    }

    @Test
    void create_ticket_with_refresh_token_in_authorization_returns_401() throws Exception {
        JsonNode loginJson = login();
        String refreshToken = loginJson.get("refreshToken").asText();

        ResponseEntity<String> res = createTicketWithToken(refreshToken);

        // Le filtre accepte uniquement typ=access
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
