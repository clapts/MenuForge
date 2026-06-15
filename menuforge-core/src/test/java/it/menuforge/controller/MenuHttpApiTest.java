package it.menuforge.controller;

import it.menuforge.dto.request.CategoryRequest;
import it.menuforge.dto.response.MenuResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MenuHttpApiTest {

    private static final Path DATA_DIR = Path.of("target/http-api-test-data-" + System.nanoTime()).toAbsolutePath();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("menuforge.enabled", () -> "true");
        registry.add("menuforge.instance-name", () -> "HTTP Test Restaurant");
        registry.add("menuforge.data-dir", () -> DATA_DIR.toString());
        registry.add("menuforge.menu-file", () -> "menu.json");
        registry.add("menuforge.backup-on-write", () -> "false");
        registry.add("menuforge.api.admin.enabled", () -> "true");
        registry.add("menuforge.api.admin.api-key", () -> "test-http-key");
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Test
    void publicMenuIsReadable() {
        ResponseEntity<MenuResponse> response = rest.getForEntity(url("/api/menu"), MenuResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getInstanceName()).isEqualTo("HTTP Test Restaurant");
    }

    @Test
    void adminApiRequiresApiKey() {
        ResponseEntity<String> response = rest.getForEntity(url("/api/menu/admin/export"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void adminApiAcceptsValidApiKeyAndWritesData() {
        CategoryRequest request = new CategoryRequest();
        request.setTitle("Cocktail");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-MenuForge-Key", "test-http-key");

        ResponseEntity<String> createResponse = rest.exchange(
                url("/api/menu/admin/categories"),
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                String.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<MenuResponse> menuResponse = rest.getForEntity(url("/api/menu"), MenuResponse.class);
        assertThat(menuResponse.getBody()).isNotNull();
        assertThat(menuResponse.getBody().getCategories())
                .anyMatch(category -> category.getSlug().equals("cocktail"));
    }

    @Test
    void adminApiReturnsConflictForDuplicateCategory() {
        CategoryRequest request = new CategoryRequest();
        request.setSlug("duplicate");
        request.setTitle("Duplicate");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-MenuForge-Key", "test-http-key");

        rest.exchange(url("/api/menu/admin/categories"), HttpMethod.POST, new HttpEntity<>(request, headers), String.class);
        ResponseEntity<String> duplicateResponse = rest.exchange(
                url("/api/menu/admin/categories"),
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                String.class);

        assertThat(duplicateResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(duplicateResponse.getBody()).contains("Duplicate Menu Resource");
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
