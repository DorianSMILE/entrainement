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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TicketApiTest {

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

        // Flyway gère le schéma
        r.add("spring.flyway.enabled", () -> "true");
        // Hibernate ne doit pas créer/modifier le schéma
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");

        r.add("security.jwt.secret", () -> "c3VwZXItc2VjcmV0LXRlc3Qtc2VjcmV0LXRlc3Qtc2VjcmV0LTEyMw==");
        r.add("security.jwt.expiration-minutes", () -> "60");
    }

    @Autowired TestRestTemplate rest;
    @Autowired ObjectMapper om;

    private String loginAndGetToken() throws Exception {
        Map<String, String> loginBody = Map.of(
                "username", USER,
                "password", PASS
        );

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> resp = rest.exchange(
                "/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(loginBody, h),
                String.class
        );

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotBlank();

        JsonNode json = om.readTree(resp.getBody());

        assertThat(json.hasNonNull("token")).isTrue();
        assertThat(json.hasNonNull("tokenType")).isTrue();
        assertThat(json.get("tokenType").asText()).isEqualTo("Bearer");
        assertThat(json.hasNonNull("expiresInMinutes")).isTrue();
        assertThat(json.get("expiresInMinutes").asLong()).isGreaterThan(0);

        return json.get("token").asText();
    }

    private static HttpHeaders bearerJsonHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }

    private static HttpHeaders bearerHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    void full_flow_create_get_patch_delete() throws Exception {
        String token = loginAndGetToken();

        // --- CREATE (POST /tickets) - JWT requis
        Map<String, Object> createBody = Map.of(
                "title", "Ticket IT",
                "description", "From integration test",
                "priority", "MEDIUM"
        );

        HttpEntity<Map<String, Object>> createReq = new HttpEntity<>(createBody, bearerJsonHeaders(token));
        ResponseEntity<String> created = rest.exchange("/tickets", HttpMethod.POST, createReq, String.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotBlank();

        JsonNode createdJson = om.readTree(created.getBody());
        UUID id = UUID.fromString(createdJson.get("id").asText());

        // --- GET /tickets/{id} - public
        ResponseEntity<String> got = rest.getForEntity("/tickets/" + id, String.class);
        assertThat(got.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode gotJson = om.readTree(got.getBody());
        assertThat(gotJson.get("title").asText()).isEqualTo("Ticket IT");

        // --- PATCH /tickets/{id} - JWT requis
        Map<String, Object> patchBody = Map.of("title", "Ticket IT updated");
        HttpEntity<Map<String, Object>> patchReq = new HttpEntity<>(patchBody, bearerJsonHeaders(token));

        ResponseEntity<String> patched = rest.exchange("/tickets/" + id, HttpMethod.PATCH, patchReq, String.class);
        assertThat(patched.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode patchedJson = om.readTree(patched.getBody());
        assertThat(patchedJson.get("title").asText()).isEqualTo("Ticket IT updated");

        // --- LIST /tickets?q=... - public
        ResponseEntity<String> list = rest.getForEntity("/tickets?q=updated", String.class);
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);

        // --- DELETE /tickets/{id} - JWT requis
        HttpEntity<Void> delReq = new HttpEntity<>(bearerHeaders(token));
        ResponseEntity<Void> deleted = rest.exchange("/tickets/" + id, HttpMethod.DELETE, delReq, Void.class);
        assertThat(deleted.getStatusCode()).isIn(HttpStatus.NO_CONTENT, HttpStatus.OK);

        // --- GET après delete -> 404
        ResponseEntity<String> afterDelete = rest.getForEntity("/tickets/" + id, String.class);
        assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void create_requires_jwt() throws Exception {
        // payload valide
        Map<String, Object> body = Map.of(
                "title", "Security check",
                "description", "Should require JWT",
                "priority", "MEDIUM"
        );

        HttpHeaders noAuthHeaders = new HttpHeaders();
        noAuthHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> withoutToken = rest.exchange(
                "/tickets",
                HttpMethod.POST,
                new HttpEntity<>(body, noAuthHeaders),
                String.class
        );

        assertThat(withoutToken.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        String token = loginAndGetToken();

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
        authHeaders.setBearerAuth(token);

        ResponseEntity<String> withToken = rest.exchange(
                "/tickets",
                HttpMethod.POST,
                new HttpEntity<>(body, authHeaders),
                String.class
        );

        assertThat(withToken.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(withToken.getBody()).isNotBlank();

        JsonNode createdJson = om.readTree(withToken.getBody());
        assertThat(createdJson.get("id").asText()).isNotBlank();
    }
}
