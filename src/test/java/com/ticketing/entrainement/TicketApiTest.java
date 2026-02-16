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

        r.add("spring.flyway.enabled", () -> "true");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");

        r.add("security.jwt.access-secret", () -> "c3VwZXItc2VjcmV0LXRlc3QtYWNjZXNzLXNlY3JldC0xMjM0NTY3ODkw");
        r.add("security.jwt.refresh-secret", () -> "c3VwZXItc2VjcmV0LXRlc3QtcmVmcmVzaC1zZWNyZXQtMTIzNDU2Nzg5MA==");
        r.add("security.jwt.access-expiration-minutes", () -> "60");
        r.add("security.jwt.refresh-expiration-days", () -> "7");
    }

    @Autowired TestRestTemplate rest;
    @Autowired ObjectMapper om;

    private String loginAndGetAccessToken() throws Exception {
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

        assertThat(resp.getStatusCode())
                .as("Login status, body=%s", resp.getBody())
                .isEqualTo(HttpStatus.OK);

        assertThat(resp.getBody()).isNotBlank();

        JsonNode json = om.readTree(resp.getBody());

        assertThat(json.has("accessToken"))
                .as("Login JSON=%s", resp.getBody())
                .isTrue();

        assertThat(json.get("accessToken").isTextual())
                .as("accessToken absent/invalid, JSON=%s", resp.getBody())
                .isTrue();

        String token = json.get("accessToken").asText();
        assertThat(token).isNotBlank();

        return token;
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
        String token = loginAndGetAccessToken();

        // CREATE (JWT requis)
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

        // GET /tickets/{id} (public)
        HttpEntity<Void> getReq = new HttpEntity<>(bearerHeaders(token));

        ResponseEntity<String> got = rest.exchange(
                "/tickets/" + id,
                HttpMethod.GET,
                getReq,
                String.class
        );

        assertThat(got.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode gotJson = om.readTree(got.getBody());
        assertThat(gotJson.get("title").asText()).isEqualTo("Ticket IT");

        // PATCH /tickets/{id} (JWT requis)
        Map<String, Object> patchBody = Map.of("title", "Ticket IT updated");
        HttpEntity<Map<String, Object>> patchReq = new HttpEntity<>(patchBody, bearerJsonHeaders(token));

        ResponseEntity<String> patched = rest.exchange("/tickets/" + id, HttpMethod.PATCH, patchReq, String.class);
        assertThat(patched.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode patchedJson = om.readTree(patched.getBody());
        assertThat(patchedJson.get("title").asText()).isEqualTo("Ticket IT updated");

        // LIST /tickets?q=... (public)
        ResponseEntity<String> list = rest.exchange(
                "/tickets?q=updated",
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(token)),
                String.class
        );
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);

        // DELETE /tickets/{id} (JWT requis)
        HttpEntity<Void> delReq = new HttpEntity<>(bearerHeaders(token));
        ResponseEntity<Void> deleted = rest.exchange("/tickets/" + id, HttpMethod.DELETE, delReq, Void.class);
        assertThat(deleted.getStatusCode()).isIn(HttpStatus.NO_CONTENT, HttpStatus.OK);

        // GET après delete => 404
        ResponseEntity<String> afterDelete = rest.exchange(
                "/tickets/" + id,
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(token)),
                String.class
        );
        assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
