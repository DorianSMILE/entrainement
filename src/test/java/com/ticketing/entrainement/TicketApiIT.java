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
class TicketApiIT {

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

        // Important: on laisse Flyway créer le schéma
        r.add("spring.flyway.enabled", () -> "true");
        // Et on évite Hibernate schema auto
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired TestRestTemplate rest;
    @Autowired ObjectMapper om;

    @Test
    void full_flow_create_get_patch_delete() throws Exception {
        // --- CREATE (POST /tickets) - nécessite auth
        var createBody = Map.of(
                "title", "Ticket IT",
                "description", "From integration test",
                "priority", "MEDIUM"
        );

        ResponseEntity<String> created = rest
                .withBasicAuth("admin", "admin")
                .postForEntity("/tickets", createBody, String.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        JsonNode createdJson = om.readTree(created.getBody());
        UUID id = UUID.fromString(createdJson.get("id").asText());

        // --- GET /tickets/{id} - public
        ResponseEntity<String> got = rest.getForEntity("/tickets/" + id, String.class);
        assertThat(got.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode gotJson = om.readTree(got.getBody());
        assertThat(gotJson.get("title").asText()).isEqualTo("Ticket IT");

        Map<String, Object> patchBody = Map.of("title", "Ticket IT updated");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> patchReq = new HttpEntity<>(patchBody, headers);

        ResponseEntity<String> patched = rest
                .withBasicAuth("admin", "admin")
                .exchange("/tickets/" + id, HttpMethod.PATCH, patchReq, String.class);

        assertThat(patched.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode patchedJson = om.readTree(patched.getBody());
        assertThat(patchedJson.get("title").asText()).isEqualTo("Ticket IT updated");

        // --- LIST /tickets?q=... - public
        ResponseEntity<String> list = rest.getForEntity("/tickets?q=updated", String.class);
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);

        // --- DELETE /tickets/{id} - nécessite auth
        ResponseEntity<Void> deleted = rest
                .withBasicAuth("admin", "admin")
                .exchange("/tickets/" + id, HttpMethod.DELETE, null, Void.class);

        assertThat(deleted.getStatusCode()).isIn(HttpStatus.NO_CONTENT, HttpStatus.OK);

        // --- GET après delete -> 404
        ResponseEntity<String> afterDelete = rest.getForEntity("/tickets/" + id, String.class);
        assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
