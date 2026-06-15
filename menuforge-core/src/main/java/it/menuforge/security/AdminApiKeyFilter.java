package it.menuforge.security;

import it.menuforge.MenuForgeProperties;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Servlet filter that protects MenuForge admin endpoints with an API key.
 *
 * <p>When admin endpoints are enabled ({@code menuforge.api.admin.enabled=true}),
 * this filter intercepts every HTTP request targeting the admin base path and
 * validates the {@code X-MenuForge-Key} header against the configured key.
 *
 * <h3>Security guarantees:</h3>
 * <ul>
 *   <li>The API key is <strong>never logged</strong> — not at startup, not on error.</li>
 *   <li>The API key is <strong>never included</strong> in any HTTP response or error message.</li>
 *   <li>The API key is <strong>never persisted</strong> to disk by MenuForge.</li>
 *   <li>Key comparison uses <strong>constant-time</strong> algorithm to prevent timing attacks.</li>
 *   <li>The key is held <strong>in memory only</strong>, as part of the Spring properties object.</li>
 * </ul>
 *
 * <h3>Request flow:</h3>
 * <pre>
 * Request → is admin path? → NO  → pass through (public endpoint)
 *                           → YES → has X-MenuForge-Key header?
 *                                   → NO  → 401 Unauthorized
 *                                   → YES → key matches?
 *                                           → NO  → 401 Unauthorized
 *                                           → YES → pass through
 * </pre>
 *
 * <p>This filter is registered conditionally by {@link it.menuforge.MenuForgeAutoConfiguration}
 * only when {@code menuforge.api.admin.enabled=true}.
 */
public class AdminApiKeyFilter implements Filter {

    /** The header name that must contain the API key. */
    public static final String HEADER_NAME = "X-MenuForge-Key";

    private final String adminBasePath;
    private final byte[] expectedKeyBytes;

    /**
     * Creates the filter with the configured admin path and API key.
     *
     * <p>The key is immediately converted to a byte array for constant-time
     * comparison. The original String reference from properties remains in
     * the Spring context but is never stored separately by this filter.
     *
     * @param properties the MenuForge configuration properties
     */
    public AdminApiKeyFilter(MenuForgeProperties properties) {
        this.adminBasePath = properties.getApi().getAdminBasePath();
        this.expectedKeyBytes = properties.getApi().getAdmin().getApiKey()
                .getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        String path = httpReq.getRequestURI();

        boolean adminRequest = path.equals(adminBasePath) || path.startsWith(adminBasePath + "/");
        if (!adminRequest) {
            chain.doFilter(request, response);
            return;
        }

        String providedKey = httpReq.getHeader(HEADER_NAME);

        if (providedKey == null || providedKey.isBlank()) {
            sendUnauthorized(httpResp, "Missing required header: " + HEADER_NAME);
            return;
        }

        byte[] providedKeyBytes = providedKey.getBytes(StandardCharsets.UTF_8);

        // Constant-time comparison to prevent timing attacks
        if (!MessageDigest.isEqual(expectedKeyBytes, providedKeyBytes)) {
            sendUnauthorized(httpResp, "Invalid API key");
            return;
        }

        // Key is valid — proceed
        chain.doFilter(request, response);
    }

    /**
     * Sends a 401 Unauthorized response with a JSON body following RFC 9457 Problem Detail format.
     *
     * <p>The response never includes the expected key or any hint about its value.
     */
    private void sendUnauthorized(HttpServletResponse response, String detail) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(
                "{\"type\":\"https://menuforge.it/errors/unauthorized\","
                + "\"title\":\"Unauthorized\","
                + "\"status\":401,"
                + "\"detail\":\"" + detail + "\"}"
        );
    }
}
