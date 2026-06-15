package it.menuforge;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "menuforge")
public class MenuForgeProperties {

    private boolean enabled = true;
    private String instanceName = "MenuForge";
    private String dataDir = "./menuforge-data";
    private String menuFile = "menu.json";
    private boolean backupOnWrite = true;
    private Api api = new Api();

    @Data
    public static class Api {
        private String basePath = "/api/menu";
        private String adminBasePath = "/api/menu/admin";
        private String corsOrigins = "*";
        private Admin admin = new Admin();

        @Data
        public static class Admin {
            private boolean enabled = false;
            private String apiKey;
        }
    }
}
