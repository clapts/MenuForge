package it.menuforge.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.menuforge.MenuForgeProperties;
import it.menuforge.model.MenuDocument;
import it.menuforge.model.MenuDocumentContract;
import it.menuforge.util.AllergenCatalog;
import it.menuforge.validation.MenuDocumentValidator;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;

@Component
public class JsonFileMenuStorage implements MenuStorage {

    private final MenuForgeProperties properties;
    private final ObjectMapper mapper;

    public JsonFileMenuStorage(MenuForgeProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.mapper = objectMapper.copy()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    @SneakyThrows
    public synchronized MenuDocument load() {
        Path path = menuPath();
        if (!Files.exists(path)) {
            MenuDocument created = emptyDocument();
            saveWithoutBackup(created);
            return created;
        }
        MenuDocument document = mapper.readValue(path.toFile(), MenuDocument.class);
        normalize(document);
        MenuDocumentValidator.validate(document);
        return document;
    }

    @Override
    @SneakyThrows
    public synchronized MenuDocument save(MenuDocument document) {
        normalize(document);
        MenuDocumentValidator.validate(document);
        document.setUpdatedAt(Instant.now());
        Path path = menuPath();
        Files.createDirectories(path.getParent());
        if (properties.isBackupOnWrite() && Files.exists(path)) {
            Path backup = path.resolveSibling("menu-" + System.currentTimeMillis() + ".backup.json");
            Files.copy(path, backup, StandardCopyOption.REPLACE_EXISTING);
        }
        writeAtomic(path, document);
        return document;
    }

    @SneakyThrows
    private void saveWithoutBackup(MenuDocument document) {
        normalize(document);
        MenuDocumentValidator.validate(document);
        document.setUpdatedAt(Instant.now());
        Path path = menuPath();
        Files.createDirectories(path.getParent());
        writeAtomic(path, document);
    }

    @SneakyThrows
    private void writeAtomic(Path path, MenuDocument document) {
        Path temp = path.resolveSibling(path.getFileName() + ".tmp");
        mapper.writeValue(temp.toFile(), document);
        Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    private Path menuPath() {
        return Path.of(properties.getDataDir()).resolve(properties.getMenuFile()).toAbsolutePath().normalize();
    }

    private MenuDocument emptyDocument() {
        return MenuDocument.builder()
                .instanceName(properties.getInstanceName())
                .allergens(AllergenCatalog.all())
                .build();
    }

    private void normalize(MenuDocument document) {
        if (document.getSchemaVersion() == null || document.getSchemaVersion().isBlank()) {
            document.setSchemaVersion(MenuDocumentContract.SCHEMA_VERSION);
        }
        if (document.getInstanceName() == null || document.getInstanceName().isBlank()) {
            document.setInstanceName(properties.getInstanceName());
        }
        if (document.getCategories() == null) {
            document.setCategories(new ArrayList<>());
        }
        if (document.getBadges() == null) {
            document.setBadges(new ArrayList<>());
        }
        if (document.getAllergens() == null || document.getAllergens().isEmpty()) {
            document.setAllergens(AllergenCatalog.all());
        }
        document.getCategories().forEach(category -> {
            if (category.getId() == null || category.getId().isBlank()) {
                category.setId(category.getSlug());
            }
            if (category.getItems() == null) {
                category.setItems(new ArrayList<>());
            }
            category.getItems().forEach(item -> {
                if (item.getIngredients() == null) item.setIngredients(new ArrayList<>());
                if (item.getTag1() == null) item.setTag1(new ArrayList<>());
                if (item.getTag2() == null) item.setTag2(new ArrayList<>());
                if (item.getTag3() == null) item.setTag3(new ArrayList<>());
                if (item.getBadges() == null) item.setBadges(new ArrayList<>());
                if (item.getAllergens() == null) item.setAllergens(new ArrayList<>());
                if (item.getCustomAttributes() == null) item.setCustomAttributes(new ArrayList<>());
            });
        });
    }
}
