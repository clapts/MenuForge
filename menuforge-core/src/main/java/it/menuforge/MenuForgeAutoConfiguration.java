package it.menuforge;

import jakarta.servlet.Filter;
import it.menuforge.security.AdminApiKeyFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;
import java.util.List;

@AutoConfiguration
@ConditionalOnProperty(prefix = "menuforge", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(MenuForgeProperties.class)
@ComponentScan(basePackages = "it.menuforge")
public class MenuForgeAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MenuForgeAutoConfiguration.class);

    @Bean
    public FilterRegistrationBean<Filter> menuForgeCorsFilter(MenuForgeProperties props) {
        Filter filter = (request, response, chain) -> {
            jakarta.servlet.http.HttpServletRequest httpReq = (jakarta.servlet.http.HttpServletRequest) request;
            jakarta.servlet.http.HttpServletResponse httpResp = (jakarta.servlet.http.HttpServletResponse) response;
            String path = httpReq.getRequestURI();
            boolean menuForgeRequest = path.equals(props.getApi().getBasePath())
                    || path.startsWith(props.getApi().getBasePath() + "/")
                    || path.equals(props.getApi().getAdminBasePath())
                    || path.startsWith(props.getApi().getAdminBasePath() + "/");

            if (!menuForgeRequest) {
                chain.doFilter(request, response);
                return;
            }

            String origin = httpReq.getHeader("Origin");

            if (origin != null && isAllowedOrigin(origin, props.getApi().getCorsOrigins())) {
                httpResp.setHeader("Access-Control-Allow-Origin", corsOriginValue(origin, props.getApi().getCorsOrigins()));
                httpResp.setHeader("Vary", "Origin");
                httpResp.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
                httpResp.setHeader("Access-Control-Allow-Headers", "Content-Type,X-MenuForge-Key,Authorization");
                httpResp.setHeader("Access-Control-Max-Age", "3600");
            }

            if ("OPTIONS".equalsIgnoreCase(httpReq.getMethod())) {
                httpResp.setStatus(jakarta.servlet.http.HttpServletResponse.SC_OK);
                return;
            }

            chain.doFilter(request, response);
        };

        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(0);
        return registration;
    }

    private boolean isAllowedOrigin(String origin, String configuredOrigins) {
        List<String> origins = parseOrigins(configuredOrigins);
        return origins.contains("*") || origins.contains(origin);
    }

    private List<String> parseOrigins(String origins) {
        if (origins == null || origins.isBlank()) {
            return List.of("*");
        }
        return Arrays.stream(origins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();
    }

    private String corsOriginValue(String requestOrigin, String configuredOrigins) {
        return parseOrigins(configuredOrigins).contains("*") ? "*" : requestOrigin;
    }

    @Bean
    @ConditionalOnProperty(name = "menuforge.api.admin.enabled", havingValue = "true")
    public FilterRegistrationBean<AdminApiKeyFilter> menuForgeAdminApiKeyFilter(MenuForgeProperties props) {
        String apiKey = props.getApi().getAdmin().getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "MenuForge admin HTTP API is enabled but menuforge.api.admin.api-key is empty. "
                    + "Set it from a secret environment variable, for example ${MENUFORGE_ADMIN_KEY}.");
        }

        log.info("MenuForge external admin HTTP API enabled on {}", props.getApi().getAdminBasePath());
        AdminApiKeyFilter filter = new AdminApiKeyFilter(props);
        FilterRegistrationBean<AdminApiKeyFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
}
