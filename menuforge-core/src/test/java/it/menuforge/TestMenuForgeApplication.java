package it.menuforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Test-only Spring Boot application entry point.
 *
 * <p>This class exists ONLY to satisfy {@code @WebMvcTest} and {@code @SpringBootTest}
 * in test scope. It is not included in the library JAR and is not part of the
 * public MenuForge API.
 *
 * <p>{@code @EnableConfigurationProperties} is required so that {@link MenuForgeProperties}
 * is registered as a bean even in the slim {@code @WebMvcTest} context, where
 * auto-configuration is limited and {@code @ConfigurationProperties} classes are
 * not registered automatically unless explicitly declared.
 *
 * <p>In a real integrating site, the site's own {@code @SpringBootApplication}
 * serves this role. Since MenuForge is a library (not a runnable app), we need
 * a test-only application class for the integration tests.
 */
@SpringBootApplication
@EnableConfigurationProperties(MenuForgeProperties.class)
public class TestMenuForgeApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestMenuForgeApplication.class, args);
    }
}
