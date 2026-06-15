package it.menuforge;

import it.menuforge.security.AdminApiKeyFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@ConditionalOnProperty(prefix = "menuforge", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(MenuForgeProperties.class)
@ComponentScan(basePackages = "it.menuforge")
public class MenuForgeAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MenuForgeAutoConfiguration.class);

    @Bean
    public WebMvcConfigurer menuForgeCorsConfigurer(MenuForgeProperties props) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String origins = props.getApi().getCorsOrigins();
                registry.addMapping(props.getApi().getBasePath() + "/**")
                        .allowedOrigins(origins)
                        .allowedMethods("GET", "OPTIONS");

                if (props.getApi().getAdmin().isEnabled()) {
                    registry.addMapping(props.getApi().getAdminBasePath() + "/**")
                            .allowedOrigins(origins)
                            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
                }
            }
        };
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
        registration.addUrlPatterns(props.getApi().getAdminBasePath() + "/*");
        registration.setOrder(1);
        return registration;
    }
}
