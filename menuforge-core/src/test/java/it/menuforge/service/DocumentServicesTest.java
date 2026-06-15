package it.menuforge.service;

import it.menuforge.MenuForgeProperties;
import it.menuforge.dto.request.CategoryRequest;
import it.menuforge.dto.request.MenuItemRequest;
import it.menuforge.exception.DuplicateMenuResourceException;
import it.menuforge.exception.InvalidMenuDocumentException;
import it.menuforge.model.Category;
import it.menuforge.model.MenuDocument;
import it.menuforge.model.MenuItem;
import it.menuforge.storage.JsonFileMenuStorage;
import it.menuforge.storage.MenuStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentServicesTest {

    @TempDir
    Path tempDir;

    private MenuStorage storage;
    private CategoryService categoryService;
    private MenuItemService menuItemService;
    private AllergenService allergenService;
    private MenuDocumentService menuDocumentService;

    @BeforeEach
    void setUp() {
        MenuForgeProperties properties = new MenuForgeProperties();
        properties.setInstanceName("Demo Restaurant");
        properties.setDataDir(tempDir.toString());
        properties.setBackupOnWrite(false);
        storage = new JsonFileMenuStorage(properties, Jackson2ObjectMapperBuilder.json().build());
        allergenService = new AllergenService(storage);
        categoryService = new CategoryService(storage);
        menuItemService = new MenuItemService(storage, allergenService);
        menuDocumentService = new MenuDocumentService(storage, properties);
    }

    @Test
    void createsMenuJsonOnFirstLoad() {
        assertThat(storage.load().getInstanceName()).isEqualTo("Demo Restaurant");
        assertThat(Files.exists(tempDir.resolve("menu.json"))).isTrue();
        assertThat(allergenService.getAllAllergens()).hasSize(14);
    }

    @Test
    void categoryAndItemsAreManagedAsDocumentData() {
        CategoryRequest categoryRequest = new CategoryRequest();
        categoryRequest.setTitle("Pizze Classiche");
        var category = categoryService.createCategory(categoryRequest);

        MenuItemRequest itemRequest = new MenuItemRequest();
        itemRequest.setTitle("Margherita");
        itemRequest.setPrice("6,00");
        itemRequest.setIngredients(List.of("Pomodoro", "Mozzarella"));
        itemRequest.setAllergenNumbers(List.of(1, 7));

        var item = menuItemService.createItem(category.getSlug(), itemRequest);

        assertThat(category.getSlug()).isEqualTo("pizze-classiche");
        assertThat(item.getId()).isEqualTo("margherita");
        assertThat(storage.load().getCategories().getFirst().getItems()).hasSize(1);
        assertThat(menuItemService.getItemsByCategory("pizze-classiche").getFirst().getAllergens()).hasSize(2);
    }

    @Test
    void reorderRejectsItemsFromAnotherCategory() {
        CategoryRequest first = new CategoryRequest();
        first.setTitle("Pizze");
        CategoryRequest second = new CategoryRequest();
        second.setTitle("Bevande");
        categoryService.createCategory(first);
        categoryService.createCategory(second);

        MenuItemRequest pizza = new MenuItemRequest();
        pizza.setTitle("Margherita");
        menuItemService.createItem("pizze", pizza);
        MenuItemRequest drink = new MenuItemRequest();
        drink.setTitle("Acqua");
        menuItemService.createItem("bevande", drink);

        assertThatThrownBy(() -> menuItemService.reorderItems("pizze", List.of("acqua")))
                .isInstanceOf(InvalidMenuDocumentException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    void replaceMenuRejectsDuplicateCategorySlugs() {
        MenuDocument document = MenuDocument.builder()
                .instanceName("Invalid")
                .categories(List.of(
                        Category.builder().slug("pizze").title("Pizze").items(new ArrayList<>()).build(),
                        Category.builder().slug("pizze").title("Pizze doppie").items(new ArrayList<>()).build()
                ))
                .build();

        assertThatThrownBy(() -> menuDocumentService.replaceMenu(document))
                .isInstanceOf(DuplicateMenuResourceException.class)
                .hasMessageContaining("Duplicate category slug");
    }

    @Test
    void replaceMenuRejectsDuplicateItemIdsAcrossCategories() {
        MenuItem first = MenuItem.builder().id("margherita").title("Margherita").build();
        MenuItem second = MenuItem.builder().id("margherita").title("Margherita baby").build();
        MenuDocument document = MenuDocument.builder()
                .instanceName("Invalid")
                .categories(List.of(
                        Category.builder().slug("pizze").title("Pizze").items(new ArrayList<>(List.of(first))).build(),
                        Category.builder().slug("baby").title("Baby").items(new ArrayList<>(List.of(second))).build()
                ))
                .build();

        assertThatThrownBy(() -> menuDocumentService.replaceMenu(document))
                .isInstanceOf(DuplicateMenuResourceException.class)
                .hasMessageContaining("Duplicate item id");
    }

    @Test
    void createItemRejectsGlobalDuplicateId() {
        CategoryRequest first = new CategoryRequest();
        first.setTitle("Pizze");
        CategoryRequest second = new CategoryRequest();
        second.setTitle("Fritti");
        categoryService.createCategory(first);
        categoryService.createCategory(second);

        MenuItemRequest pizza = new MenuItemRequest();
        pizza.setId("margherita");
        pizza.setTitle("Margherita");
        menuItemService.createItem("pizze", pizza);

        MenuItemRequest duplicate = new MenuItemRequest();
        duplicate.setId("margherita");
        duplicate.setTitle("Margherita fritta");

        assertThatThrownBy(() -> menuItemService.createItem("fritti", duplicate))
                .isInstanceOf(DuplicateMenuResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void writesCreateBackupsWhenEnabled() throws Exception {
        MenuForgeProperties properties = new MenuForgeProperties();
        properties.setInstanceName("Backup Restaurant");
        properties.setDataDir(tempDir.toString());
        properties.setBackupOnWrite(true);
        MenuStorage backupStorage = new JsonFileMenuStorage(properties, Jackson2ObjectMapperBuilder.json().build());
        CategoryService backupCategoryService = new CategoryService(backupStorage);

        backupStorage.load();
        CategoryRequest request = new CategoryRequest();
        request.setTitle("Bevande");
        backupCategoryService.createCategory(request);

        assertThat(Files.list(tempDir)
                .filter(path -> path.getFileName().toString().endsWith(".backup.json"))
                .count()).isGreaterThanOrEqualTo(1);
    }
}
