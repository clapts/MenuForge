package it.menuforge.controller;

import it.menuforge.dto.request.BadgeRequest;
import it.menuforge.dto.request.CategoryRequest;
import it.menuforge.dto.request.MenuItemRequest;
import it.menuforge.dto.response.CategoryResponse;
import it.menuforge.dto.response.MenuItemResponse;
import it.menuforge.model.Badge;
import it.menuforge.model.Category;
import it.menuforge.model.MenuDocument;
import it.menuforge.service.BadgeService;
import it.menuforge.service.CategoryService;
import it.menuforge.service.MenuDocumentService;
import it.menuforge.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "menuforge.api.admin.enabled", havingValue = "true")
@RequestMapping("${menuforge.api.admin-base-path:/api/menu/admin}")
public class MenuAdminController {

    private final CategoryService categoryService;
    private final MenuItemService menuItemService;
    private final BadgeService badgeService;
    private final MenuDocumentService menuDocumentService;

    @GetMapping("/export")
    public ResponseEntity<MenuDocument> exportMenu() {
        return ResponseEntity.ok(menuDocumentService.exportMenu());
    }

    @PutMapping("/import")
    public ResponseEntity<MenuDocument> replaceMenu(@RequestBody MenuDocument document) {
        return ResponseEntity.ok(menuDocumentService.replaceMenu(document));
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CategoryResponse.from(categoryService.createCategory(request), false));
    }

    @PutMapping("/categories/{slug}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable String slug,
            @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(CategoryResponse.from(categoryService.updateCategory(slug, request), false));
    }

    @PutMapping("/categories/{slug}/replace")
    public ResponseEntity<CategoryResponse> replaceCategory(
            @PathVariable String slug,
            @RequestBody Category category) {
        return ResponseEntity.ok(CategoryResponse.from(categoryService.replaceCategory(slug, category)));
    }

    @DeleteMapping("/categories/{slug}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String slug) {
        categoryService.deleteCategory(slug);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/categories/{slug}/toggle")
    public ResponseEntity<CategoryResponse> toggleCategoryVisibility(@PathVariable String slug) {
        return ResponseEntity.ok(CategoryResponse.from(categoryService.toggleVisibility(slug), false));
    }

    @PatchMapping("/categories/reorder")
    public ResponseEntity<Void> reorderCategories(@RequestBody List<String> orderedSlugs) {
        categoryService.reorderCategories(orderedSlugs);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/categories/{slug}/items")
    public ResponseEntity<MenuItemResponse> createItem(
            @PathVariable String slug,
            @RequestBody MenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MenuItemResponse.from(menuItemService.createItem(slug, request)));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<MenuItemResponse> updateItem(
            @PathVariable String id,
            @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(MenuItemResponse.from(menuItemService.updateItem(id, request)));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable String id) {
        menuItemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/items/{id}/toggle-availability")
    public ResponseEntity<MenuItemResponse> toggleAvailability(@PathVariable String id) {
        return ResponseEntity.ok(MenuItemResponse.from(menuItemService.toggleAvailability(id)));
    }

    @PatchMapping("/items/{id}/ingredients")
    public ResponseEntity<MenuItemResponse> setIngredients(
            @PathVariable String id,
            @RequestBody List<String> ingredients) {
        return ResponseEntity.ok(MenuItemResponse.from(menuItemService.setIngredients(id, ingredients)));
    }

    @PatchMapping("/items/{id}/tag1")
    public ResponseEntity<MenuItemResponse> setTag1(@PathVariable String id, @RequestBody List<String> tags) {
        return ResponseEntity.ok(MenuItemResponse.from(menuItemService.setTag1(id, tags)));
    }

    @PatchMapping("/items/{id}/tag2")
    public ResponseEntity<MenuItemResponse> setTag2(@PathVariable String id, @RequestBody List<String> tags) {
        return ResponseEntity.ok(MenuItemResponse.from(menuItemService.setTag2(id, tags)));
    }

    @PatchMapping("/items/{id}/tag3")
    public ResponseEntity<MenuItemResponse> setTag3(@PathVariable String id, @RequestBody List<String> tags) {
        return ResponseEntity.ok(MenuItemResponse.from(menuItemService.setTag3(id, tags)));
    }

    @PatchMapping("/items/{id}/allergens")
    public ResponseEntity<MenuItemResponse> setAllergens(
            @PathVariable String id,
            @RequestBody List<Integer> allergenNumbers) {
        return ResponseEntity.ok(MenuItemResponse.from(menuItemService.setAllergens(id, allergenNumbers)));
    }

    @PatchMapping("/items/{id}/badges")
    public ResponseEntity<MenuItemResponse> setBadges(
            @PathVariable String id,
            @RequestBody List<String> badgeIds) {
        return ResponseEntity.ok(MenuItemResponse.from(menuItemService.setBadges(id, badgeIds)));
    }

    @PatchMapping("/categories/{slug}/items/reorder")
    public ResponseEntity<Void> reorderItems(
            @PathVariable String slug,
            @RequestBody List<String> orderedIds) {
        menuItemService.reorderItems(slug, orderedIds);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/badges")
    public ResponseEntity<List<Badge>> getAllBadges() {
        return ResponseEntity.ok(badgeService.getAllBadges());
    }

    @PostMapping("/badges")
    public ResponseEntity<Badge> createBadge(@RequestBody BadgeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(badgeService.createBadge(request));
    }

    @PutMapping("/badges/{id}")
    public ResponseEntity<Badge> updateBadge(@PathVariable String id, @RequestBody BadgeRequest request) {
        return ResponseEntity.ok(badgeService.updateBadge(id, request));
    }

    @DeleteMapping("/badges/{id}")
    public ResponseEntity<Void> deleteBadge(@PathVariable String id) {
        badgeService.deleteBadge(id);
        return ResponseEntity.noContent().build();
    }
}
