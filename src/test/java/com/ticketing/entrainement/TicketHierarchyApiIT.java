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
class TicketHierarchyApiIT {

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

    @Autowired
    TestRestTemplate rest;

    @Autowired
    ObjectMapper om;

    private String loginAndGetToken() throws Exception {
        Map<String, String> body = Map.of(
                "username", "admin",
                "password", "admin"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> resp = rest.exchange("/auth/login", HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode json = om.readTree(resp.getBody());
        return json.get("accessToken").asText();
    }

    private UUID createTicket(String title, String token) throws Exception {
        Map<String, Object> body = Map.of(
                "title", title,
                "description", "Integration test",
                "priority", "MEDIUM"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        ResponseEntity<String> res = rest.exchange("/tickets", HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        JsonNode json = om.readTree(res.getBody());
        return UUID.fromString(json.get("id").asText());
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
    void attach_and_detach_ticket_parent_flow() throws Exception {
        String token = loginAndGetToken();

        // Création parent et child
        UUID parentId = createTicket("Parent Ticket", token);
        UUID childId = createTicket("Child Ticket", token);

        // Attach child au parent
        Map<String, Object> attachBody = Map.of("parentId", parentId);
        ResponseEntity<String> attachRes = rest.exchange(
                "/tickets/" + childId + "/parent",
                HttpMethod.PUT,
                new HttpEntity<>(attachBody, bearerJsonHeaders(token)),
                String.class
        );
        assertThat(attachRes.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Vérification que parentId est bien set
        ResponseEntity<String> getChild = rest.exchange(
                "/tickets/" + childId,
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(token)),
                String.class
        );
        assertThat(getChild.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode childJson = om.readTree(getChild.getBody());
        assertThat(childJson.has("parentId")).isTrue();
        assertThat(childJson.get("parentId").asText()).isEqualTo(parentId.toString());

        // Detach
        ResponseEntity<String> detachRes = rest.exchange(
                "/tickets/" + childId + "/parent",
                HttpMethod.DELETE,
                new HttpEntity<>(bearerHeaders(token)),
                String.class
        );
        assertThat(detachRes.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Vérification que parentId est null après detach
        ResponseEntity<String> getAfterDetach = rest.exchange(
                "/tickets/" + childId,
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(token)),
                String.class
        );
        JsonNode afterDetachJson = om.readTree(getAfterDetach.getBody());
        assertThat(afterDetachJson.has("parentId")).isTrue();
        assertThat(afterDetachJson.get("parentId").isNull()).isTrue();
    }
}