package it.menuforge.service;

import it.menuforge.dto.response.CategoryResponse;
import it.menuforge.dto.response.MenuItemResponse;
import it.menuforge.dto.response.MenuResponse;
import it.menuforge.model.Category;
import it.menuforge.storage.MenuStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuQueryService {

    private final MenuStorage storage;

    public MenuResponse getFullMenu() {
        var document = storage.load();
        List<CategoryResponse> categories = document.getCategories().stream()
                .filter(Category::isVisible)
                .sorted(Comparator.comparing(Category::getPosition))
                .map(CategoryResponse::from)
                .toList();
        return MenuResponse.of(document.getInstanceName(), categories);
    }

    public List<MenuItemResponse> searchByTag1(String tag) {
        return search(item -> item.getTag1().contains(tag));
    }

    public List<MenuItemResponse> searchByTag2(String tag) {
        return search(item -> item.getTag2().contains(tag));
    }

    public List<MenuItemResponse> searchByTag3(String tag) {
        return search(item -> item.getTag3().contains(tag));
    }

    public List<MenuItemResponse> getAvailableItems() {
        return search(item -> true);
    }

    public List<MenuItemResponse> getItemsByAllergen(int allergenNumber) {
        return search(item -> item.getAllergens().stream().anyMatch(allergen -> allergen.getId() == allergenNumber));
    }

    private List<MenuItemResponse> search(java.util.function.Predicate<it.menuforge.model.MenuItem> predicate) {
        return storage.load().getCategories().stream()
                .filter(Category::isVisible)
                .sorted(Comparator.comparing(Category::getPosition))
                .flatMap(category -> category.getItems().stream()
                        .filter(item -> item.isAvailable())
                        .filter(predicate)
                        .sorted(Comparator.comparing(it.menuforge.model.MenuItem::getPosition))
                        .map(item -> MenuItemResponse.from(item, category)))
                .toList();
    }
}
