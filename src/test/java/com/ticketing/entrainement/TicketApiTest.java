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
    }

    @Autowired TestRestTemplate rest;
    @Autowired ObjectMapper om;

    private static HttpHeaders authJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(USER, PASS);
        return headers;
    }

    private static HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(USER, PASS);
        return headers;
    }

    @Test
    void full_flow_create_get_patch_delete() throws Exception {
        // --- CREATE (POST /tickets) - nécessite auth
        Map<String, Object> createBody = Map.of(
                "title", "Ticket IT",
                "description", "From integration test",
                "priority", "MEDIUM"
        );

        HttpEntity<Map<String, Object>> createReq = new HttpEntity<>(createBody, authJsonHeaders());
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

        // --- PATCH /tickets/{id} - nécessite auth
        Map<String, Object> patchBody = Map.of("title", "Ticket IT updated");
        HttpEntity<Map<String, Object>> patchReq = new HttpEntity<>(patchBody, authJsonHeaders());

        ResponseEntity<String> patched = rest.exchange("/tickets/" + id, HttpMethod.PATCH, patchReq, String.class);
        assertThat(patched.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode patchedJson = om.readTree(patched.getBody());
        assertThat(patchedJson.get("title").asText()).isEqualTo("Ticket IT updated");

        // --- LIST /tickets?q=... - public
        ResponseEntity<String> list = rest.getForEntity("/tickets?q=updated", String.class);
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);

        // --- DELETE /tickets/{id} - nécessite auth
        HttpEntity<Void> delReq = new HttpEntity<>(authHeaders());
        ResponseEntity<Void> deleted = rest.exchange("/tickets/" + id, HttpMethod.DELETE, delReq, Void.class);

        assertThat(deleted.getStatusCode()).isIn(HttpStatus.NO_CONTENT, HttpStatus.OK);

        // --- GET après delete -> 404
        ResponseEntity<String> afterDelete = rest.getForEntity("/tickets/" + id, String.class);
        assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
