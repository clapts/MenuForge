package it.menuforge.service;

import it.menuforge.dto.request.MenuItemRequest;
import it.menuforge.exception.CategoryNotFoundException;
import it.menuforge.exception.DuplicateMenuResourceException;
import it.menuforge.exception.InvalidMenuDocumentException;
import it.menuforge.exception.MenuItemNotFoundException;
import it.menuforge.model.*;
import it.menuforge.storage.MenuStorage;
import it.menuforge.util.MenuForgeIds;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuStorage storage;
    private final AllergenService allergenService;

    public List<MenuItem> getItemsByCategory(String categorySlug) {
        Category category = getCategory(storage.load(), categorySlug);
        return category.getItems().stream()
                .sorted(Comparator.comparing(MenuItem::getPosition))
                .toList();
    }

    public Optional<MenuItem> getItemById(String id) {
        return storage.load().getCategories().stream()
                .flatMap(category -> category.getItems().stream())
                .filter(item -> item.getId().equals(id))
                .findFirst();
    }

    public Optional<MenuItem> getItemById(Long id) {
        return getItemById(String.valueOf(id));
    }

    public MenuItem getItemByIdOrThrow(String id) {
        return getItemById(id).orElseThrow(() -> new MenuItemNotFoundException(id));
    }

    public MenuItem getItemByIdOrThrow(Long id) {
        return getItemByIdOrThrow(String.valueOf(id));
    }

    public MenuItem createItem(String categorySlug, MenuItemRequest request) {
        requireText(request.getTitle(), "Item title is required");
        MenuDocument document = storage.load();
        Category category = getCategory(document, categorySlug);
        String id = request.getId() == null || request.getId().isBlank()
                ? MenuForgeIds.uniqueSlug(request.getTitle(), category.getItems(), MenuItem::getId)
                : MenuForgeIds.slugify(request.getId());
        ensureItemIdAvailable(document, id, null);

        MenuItem item = MenuItem.builder()
                .id(id)
                .title(request.getTitle())
                .price(request.getPrice())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .highlight(request.getHighlight() != null && request.getHighlight())
                .available(request.getAvailable() == null || request.getAvailable())
                .position(request.getPosition() != null ? request.getPosition() : category.getItems().size())
                .calories(request.getCalories())
                .origin(request.getOrigin())
                .format(request.getFormat())
                .specialText1(request.getSpecialText1())
                .specialText2(request.getSpecialText2())
                .specialText3(request.getSpecialText3())
                .ingredients(copyList(request.getIngredients()))
                .tag1(copyList(request.getTag1()))
                .tag2(copyList(request.getTag2()))
                .tag3(copyList(request.getTag3()))
                .allergens(resolveAllergens(request.getAllergenNumbers()))
                .badges(resolveBadges(document, request.getBadgeIds()))
                .build();
        category.getItems().add(item);
        storage.save(document);
        return item;
    }

    public MenuItem updateItem(String id, MenuItemRequest request) {
        MenuDocument document = storage.load();
        ItemLocation location = findItem(document, id);
        MenuItem item = location.item();
        if (request.getId() != null && !request.getId().isBlank()) {
            String newId = MenuForgeIds.slugify(request.getId());
            ensureItemIdAvailable(document, newId, item.getId());
            item.setId(newId);
        }
        if (request.getTitle() != null) item.setTitle(request.getTitle());
        if (request.getPrice() != null) item.setPrice(request.getPrice());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getImageUrl() != null) item.setImageUrl(request.getImageUrl());
        if (request.getHighlight() != null) item.setHighlight(request.getHighlight());
        if (request.getAvailable() != null) item.setAvailable(request.getAvailable());
        if (request.getPosition() != null) item.setPosition(request.getPosition());
        if (request.getCalories() != null) item.setCalories(request.getCalories());
        if (request.getOrigin() != null) item.setOrigin(request.getOrigin());
        if (request.getFormat() != null) item.setFormat(request.getFormat());
        if (request.getSpecialText1() != null) item.setSpecialText1(request.getSpecialText1());
        if (request.getSpecialText2() != null) item.setSpecialText2(request.getSpecialText2());
        if (request.getSpecialText3() != null) item.setSpecialText3(request.getSpecialText3());
        if (request.getIngredients() != null) item.setIngredients(copyList(request.getIngredients()));
        if (request.getTag1() != null) item.setTag1(copyList(request.getTag1()));
        if (request.getTag2() != null) item.setTag2(copyList(request.getTag2()));
        if (request.getTag3() != null) item.setTag3(copyList(request.getTag3()));
        if (request.getAllergenNumbers() != null) item.setAllergens(resolveAllergens(request.getAllergenNumbers()));
        if (request.getBadgeIds() != null) item.setBadges(resolveBadges(document, request.getBadgeIds()));
        storage.save(document);
        return item;
    }

    public MenuItem updateItem(Long id, MenuItemRequest request) {
        return updateItem(String.valueOf(id), request);
    }

    public void deleteItem(String id) {
        MenuDocument document = storage.load();
        for (Category category : document.getCategories()) {
            boolean removed = category.getItems().removeIf(item -> item.getId().equals(id));
            if (removed) {
                storage.save(document);
                return;
            }
        }
        throw new MenuItemNotFoundException(id);
    }

    public void deleteItem(Long id) {
        deleteItem(String.valueOf(id));
    }

    public MenuItem toggleAvailability(String id) {
        MenuDocument document = storage.load();
        MenuItem item = findItem(document, id).item();
        item.setAvailable(!item.isAvailable());
        storage.save(document);
        return item;
    }

    public MenuItem toggleAvailability(Long id) {
        return toggleAvailability(String.valueOf(id));
    }

    public void reorderItems(String categorySlug, List<String> orderedIds) {
        MenuDocument document = storage.load();
        Category category = getCategory(document, categorySlug);
        AtomicInteger position = new AtomicInteger(0);
        for (String itemId : orderedIds) {
            MenuItem item = category.getItems().stream()
                    .filter(candidate -> candidate.getId().equals(itemId))
                    .findFirst()
                    .orElseThrow(() -> new InvalidMenuDocumentException(
                            "Item '" + itemId + "' does not belong to category '" + categorySlug + "'"));
            item.setPosition(position.getAndIncrement());
        }
        storage.save(document);
    }

    public void reorderItems(String categorySlug, List<Long> orderedIds, boolean numericCompatibility) {
        reorderItems(categorySlug, orderedIds.stream().map(String::valueOf).toList());
    }

    public MenuItem setIngredients(String id, List<String> ingredients) {
        return updateCollection(id, item -> item.setIngredients(copyList(ingredients)));
    }

    public MenuItem setIngredients(Long id, List<String> ingredients) {
        return setIngredients(String.valueOf(id), ingredients);
    }

    public MenuItem setTag1(String id, List<String> tags) {
        return updateCollection(id, item -> item.setTag1(copyList(tags)));
    }

    public MenuItem setTag1(Long id, List<String> tags) {
        return setTag1(String.valueOf(id), tags);
    }

    public MenuItem setTag2(String id, List<String> tags) {
        return updateCollection(id, item -> item.setTag2(copyList(tags)));
    }

    public MenuItem setTag2(Long id, List<String> tags) {
        return setTag2(String.valueOf(id), tags);
    }

    public MenuItem setTag3(String id, List<String> tags) {
        return updateCollection(id, item -> item.setTag3(copyList(tags)));
    }

    public MenuItem setTag3(Long id, List<String> tags) {
        return setTag3(String.valueOf(id), tags);
    }

    public MenuItem setAllergens(String id, List<Integer> allergenNumbers) {
        return updateCollection(id, item -> item.setAllergens(resolveAllergens(allergenNumbers)));
    }

    public MenuItem setAllergens(Long id, List<Integer> allergenNumbers) {
        return setAllergens(String.valueOf(id), allergenNumbers);
    }

    public MenuItem setBadges(String id, List<String> badgeIds) {
        MenuDocument document = storage.load();
        MenuItem item = findItem(document, id).item();
        item.setBadges(resolveBadges(document, badgeIds));
        storage.save(document);
        return item;
    }

    public MenuItem setCustomAttribute(String id, String key, String value, String displayLabel) {
        requireText(key, "Custom attribute key is required");
        MenuDocument document = storage.load();
        MenuItem item = findItem(document, id).item();
        item.getCustomAttributes().removeIf(attribute -> attribute.getKey().equals(key));
        item.getCustomAttributes().add(new CustomAttribute(key, value, displayLabel));
        storage.save(document);
        return item;
    }

    public void removeCustomAttribute(String id, String key) {
        MenuDocument document = storage.load();
        MenuItem item = findItem(document, id).item();
        item.getCustomAttributes().removeIf(attribute -> attribute.getKey().equals(key));
        storage.save(document);
    }

    private MenuItem updateCollection(String id, java.util.function.Consumer<MenuItem> updater) {
        MenuDocument document = storage.load();
        MenuItem item = findItem(document, id).item();
        updater.accept(item);
        storage.save(document);
        return item;
    }

    private Category getCategory(MenuDocument document, String slug) {
        return document.getCategories().stream()
                .filter(category -> category.getSlug().equals(slug))
                .findFirst()
                .orElseThrow(() -> new CategoryNotFoundException(slug));
    }

    private ItemLocation findItem(MenuDocument document, String id) {
        for (Category category : document.getCategories()) {
            for (MenuItem item : category.getItems()) {
                if (item.getId().equals(id)) {
                    return new ItemLocation(category, item);
                }
            }
        }
        throw new MenuItemNotFoundException(id);
    }

    private void ensureItemIdAvailable(MenuDocument document, String id, String currentId) {
        boolean exists = document.getCategories().stream()
                .flatMap(category -> category.getItems().stream())
                .anyMatch(item -> item.getId().equals(id) && !item.getId().equals(currentId));
        if (exists) {
            throw new DuplicateMenuResourceException("An item with id '" + id + "' already exists");
        }
    }

    private List<Allergen> resolveAllergens(List<Integer> numbers) {
        if (numbers == null) {
            return new ArrayList<>();
        }
        return numbers.stream()
                .map(number -> allergenService.getAllergenById(number)
                        .orElseThrow(() -> new InvalidMenuDocumentException("Invalid allergen number: " + number)))
                .toList();
    }

    private List<Badge> resolveBadges(MenuDocument document, List<String> badgeIds) {
        if (badgeIds == null) {
            return new ArrayList<>();
        }
        return badgeIds.stream()
                .map(id -> document.getBadges().stream()
                        .filter(badge -> badge.getId().equals(id))
                        .findFirst()
                        .orElseThrow(() -> new it.menuforge.exception.BadgeNotFoundException(id)))
                .toList();
    }

    private List<String> copyList(List<String> values) {
        return values == null ? new ArrayList<>() : new ArrayList<>(values);
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidMenuDocumentException(message);
        }
    }

    private record ItemLocation(Category category, MenuItem item) {
    }
}
