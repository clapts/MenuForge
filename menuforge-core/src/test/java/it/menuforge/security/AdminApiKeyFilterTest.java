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
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DisplayName("AdminApiKeyFilter security tests")
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
    void validKeyPassesThrough() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", ADMIN_PATH + "/categories");
        request.addHeader(AdminApiKeyFilter.HEADER_NAME, VALID_KEY);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void missingHeaderReturns401() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", ADMIN_PATH + "/categories");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Missing required header");
        assertThat(response.getContentAsString()).doesNotContain(VALID_KEY);
    }

    @Test
    void wrongKeyReturns401() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", ADMIN_PATH + "/categories");
        request.addHeader(AdminApiKeyFilter.HEADER_NAME, "wrong-key-trying-to-hack");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Invalid API key");
        assertThat(response.getContentAsString()).doesNotContain(VALID_KEY);
    }

    @Test
    void emptyHeaderReturns401() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", ADMIN_PATH + "/categories");
        request.addHeader(AdminApiKeyFilter.HEADER_NAME, "");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void blankHeaderReturns401() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", ADMIN_PATH + "/categories");
        request.addHeader(AdminApiKeyFilter.HEADER_NAME, "   ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void publicPathPassesWithoutKey() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/menu");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void publicCategoriesPathPassesWithoutKey() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/menu/categories");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void optionsPreflightPassesWithoutApiKey() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", ADMIN_PATH + "/categories");
        request.addHeader("Origin", "http://127.0.0.1:19090");
        request.addHeader("Access-Control-Request-Method", "POST");
        request.addHeader("Access-Control-Request-Headers", "content-type,x-menuforge-key");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void deleteAdminWithoutKeyReturns401() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", ADMIN_PATH + "/items/42");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void unauthorizedResponseIsProblemJson() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", ADMIN_PATH + "/categories");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        String body = response.getContentAsString();
        assertThat(body).contains("\"status\":401");
        assertThat(body).contains("\"title\":\"Unauthorized\"");
        assertThat(response.getContentType()).startsWith("application/json");
    }
}
