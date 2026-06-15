package it.menuforge.service;

import it.menuforge.MenuForgeProperties;
import it.menuforge.model.Category;
import it.menuforge.model.MenuDocument;
import it.menuforge.model.MenuDocumentContract;
import it.menuforge.storage.MenuStorage;
import it.menuforge.util.AllergenCatalog;
import it.menuforge.util.MenuForgeIds;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class MenuDocumentService {

    private final MenuStorage storage;
    private final MenuForgeProperties properties;

    public MenuDocument exportMenu() {
        return storage.load();
    }

    public MenuDocument replaceMenu(MenuDocument document) {
        normalize(document);
        return storage.save(document);
    }

    private void normalize(MenuDocument document) {
        if (document.getSchemaVersion() == null || document.getSchemaVersion().isBlank()) {
            document.setSchemaVersion(MenuDocumentContract.SCHEMA_VERSION);
        }
        if (document.getInstanceName() == null || document.getInstanceName().isBlank()) {
            document.setInstanceName(properties.getInstanceName());
        }
        if (document.getAllergens() == null || document.getAllergens().isEmpty()) {
            document.setAllergens(AllergenCatalog.all());
        }
        if (document.getCategories() == null) {
            document.setCategories(new java.util.ArrayList<>());
        }
        AtomicInteger categoryPosition = new AtomicInteger(0);
        for (Category category : document.getCategories()) {
            if (category.getTitle() == null || category.getTitle().isBlank()) {
                throw new IllegalArgumentException("Every category must have a title");
            }
            String slug = category.getSlug() == null || category.getSlug().isBlank()
                    ? MenuForgeIds.slugify(category.getTitle())
                    : MenuForgeIds.slugify(category.getSlug());
            category.setSlug(slug);
            category.setId(slug);
            category.setPosition(category.getPosition() < 0 ? categoryPosition.get() : category.getPosition());
            categoryPosition.incrementAndGet();
            if (category.getItems() == null) {
                category.setItems(new java.util.ArrayList<>());
            }
            AtomicInteger itemPosition = new AtomicInteger(0);
            category.getItems().forEach(item -> {
                if (item.getTitle() == null || item.getTitle().isBlank()) {
                    throw new IllegalArgumentException("Every item must have a title");
                }
                if (item.getId() == null || item.getId().isBlank()) {
                    item.setId(MenuForgeIds.uniqueSlug(item.getTitle(), category.getItems(), it.menuforge.model.MenuItem::getId));
                } else {
                    item.setId(MenuForgeIds.slugify(item.getId()));
                }
                if (item.getPosition() < 0) {
                    item.setPosition(itemPosition.get());
                }
                itemPosition.incrementAndGet();
            });
        }
    }
}
