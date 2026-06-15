package it.menuforge.security;

import it.menuforge.MenuForgeProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AdminApiKeyFilter}.
 *
 * <p>Tests all security scenarios:
 * <ul>
 *   <li>Valid API key → request passes through</li>
 *   <li>Missing header → 401 Unauthorized</li>
 *   <li>Wrong API key → 401 Unauthorized</li>
 *   <li>Empty header → 401 Unauthorized</li>
 *   <li>Public paths → always pass through (no key required)</li>
 * </ul>
 */
@DisplayName("AdminApiKeyFilter — Security Tests")
class AdminApiKeyFilterTest {

    private static final String VALID_KEY = "test-secret-key-2025";
    private static final String ADMIN_PATH = "/api/menu/admin";

    private AdminApiKeyFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        MenuForgeProperties props = new MenuForgeProperties();
        props.getApi().setAdminBasePath(ADMIN_PATH);
        props.getApi().getAdmin().setEnabled(true);
        props.getApi().getAdmin().setApiKey(VALID_KEY);

        filter = new AdminApiKeyFilter(props);
        filterChain = mock(FilterChain.class);
    }

    @Test
    @DisplayName("Valid API key → request passes through (200)")
    void validKey_passesThrough() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", ADMIN_PATH + "/categories");
        request.addHeader(AdminApiKeyFilter.HEADER_NAME, VALID_KEY);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("Missing header → 401 Unauthorized")
    void missingHeader_returns401() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", ADMIN_PATH + "/categories");
        // No header set
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Missing required header");
        assertThat(response.getContentAsString()).doesNotContain(VALID_KEY);
    }

    @Test
    @DisplayName("Wrong API key → 401 Unauthorized")
    void wrongKey_returns401() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", ADMIN_PATH + "/categories");
        request.addHeader(AdminApiKeyFilter.HEADER_NAME, "wrong-key-trying-to-hack");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Invalid API key");
        // CRITICAL: the error response must NEVER contain the correct key
        assertThat(response.getContentAsString()).doesNotContain(VALID_KEY);
    }

    @Test
    @DisplayName("Empty header → 401 Unauthorized")
    void emptyHeader_returns401() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", ADMIN_PATH + "/categories");
        request.addHeader(AdminApiKeyFilter.HEADER_NAME, "");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("Blank header (spaces only) → 401 Unauthorized")
    void blankHeader_returns401() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", ADMIN_PATH + "/categories");
        request.addHeader(AdminApiKeyFilter.HEADER_NAME, "   ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("Public path (non-admin) → passes through without key")
    void publicPath_passesWithoutKey() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/menu");
        // No header set — this is a public endpoint
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("Public categories path → passes through without key")
    void publicCategoriesPath_passesWithoutKey() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/menu/categories");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("DELETE on admin path without key → 401")
    void deleteAdmin_withoutKey_returns401() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", ADMIN_PATH + "/items/42");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("Response body is valid JSON with RFC 9457 format")
    void unauthorizedResponse_isValidJson() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", ADMIN_PATH + "/categories");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        String body = response.getContentAsString();
        assertThat(body).contains("\"status\":401");
        assertThat(body).contains("\"title\":\"Unauthorized\"");
        assertThat(response.getContentType()).startsWith("application/json");
    }
}
