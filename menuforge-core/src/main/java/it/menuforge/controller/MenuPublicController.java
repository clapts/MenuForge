package it.menuforge.controller;

import it.menuforge.dto.response.AllergenResponse;
import it.menuforge.dto.response.CategoryResponse;
import it.menuforge.dto.response.MenuItemResponse;
import it.menuforge.dto.response.MenuResponse;
import it.menuforge.exception.CategoryNotFoundException;
import it.menuforge.exception.MenuItemNotFoundException;
import it.menuforge.service.AllergenService;
import it.menuforge.service.CategoryService;
import it.menuforge.service.MenuItemService;
import it.menuforge.service.MenuQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${menuforge.api.base-path:/api/menu}")
public class MenuPublicController {

    private final MenuQueryService menuQueryService;
    private final CategoryService categoryService;
    private final MenuItemService menuItemService;
    private final AllergenService allergenService;

    @GetMapping("")
    public ResponseEntity<MenuResponse> getFullMenu() {
        return ResponseEntity.ok(menuQueryService.getFullMenu());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(categoryService.getVisibleCategories().stream()
                .map(category -> CategoryResponse.from(category, false))
                .toList());
    }

    @GetMapping("/categories/{slug}")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(@PathVariable String slug) {
        return categoryService.getCategoryBySlug(slug)
                .map(CategoryResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new CategoryNotFoundException(slug));
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<MenuItemResponse> getItemById(@PathVariable String id) {
        return menuItemService.getItemById(id)
                .map(MenuItemResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new MenuItemNotFoundException(id));
    }

    @GetMapping("/allergens")
    public ResponseEntity<List<AllergenResponse>> getAllergens() {
        return ResponseEntity.ok(allergenService.getAllAllergens().stream()
                .map(AllergenResponse::from)
                .toList());
    }

    @GetMapping("/search")
    public ResponseEntity<List<MenuItemResponse>> search(
            @RequestParam(required = false) String tag1,
            @RequestParam(required = false) String tag2,
            @RequestParam(required = false) String tag3) {
        if (tag1 != null) return ResponseEntity.ok(menuQueryService.searchByTag1(tag1));
        if (tag2 != null) return ResponseEntity.ok(menuQueryService.searchByTag2(tag2));
        if (tag3 != null) return ResponseEntity.ok(menuQueryService.searchByTag3(tag3));
        return ResponseEntity.ok(menuQueryService.getAvailableItems());
    }
}
