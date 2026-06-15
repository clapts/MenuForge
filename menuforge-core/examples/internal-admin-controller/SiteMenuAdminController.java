package com.example.restaurant.admin;

import it.menuforge.dto.request.CategoryRequest;
import it.menuforge.dto.request.MenuItemRequest;
import it.menuforge.model.Category;
import it.menuforge.model.MenuDocument;
import it.menuforge.model.MenuItem;
import it.menuforge.service.CategoryService;
import it.menuforge.service.MenuDocumentService;
import it.menuforge.service.MenuItemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Example only.
 *
 * This controller belongs to the host website, not to MenuForge.
 * Protect it with the website's normal authentication/authorization.
 */
@RestController
@RequestMapping("/admin/menu")
public class SiteMenuAdminController {

    private final CategoryService categoryService;
    private final MenuItemService menuItemService;
    private final MenuDocumentService menuDocumentService;

    public SiteMenuAdminController(
            CategoryService categoryService,
            MenuItemService menuItemService,
            MenuDocumentService menuDocumentService) {
        this.categoryService = categoryService;
        this.menuItemService = menuItemService;
        this.menuDocumentService = menuDocumentService;
    }

    @GetMapping("/export")
    public MenuDocument exportMenu() {
        return menuDocumentService.exportMenu();
    }

    @PutMapping("/import")
    public MenuDocument replaceMenu(@RequestBody MenuDocument document) {
        return menuDocumentService.replaceMenu(document);
    }

    @PostMapping("/categories")
    public Category createCategory(@RequestBody CategoryRequest request) {
        return categoryService.createCategory(request);
    }

    @PutMapping("/categories/{slug}")
    public Category updateCategory(@PathVariable String slug, @RequestBody CategoryRequest request) {
        return categoryService.updateCategory(slug, request);
    }

    @DeleteMapping("/categories/{slug}")
    public void deleteCategory(@PathVariable String slug) {
        categoryService.deleteCategory(slug);
    }

    @PostMapping("/categories/{slug}/items")
    public MenuItem createItem(@PathVariable String slug, @RequestBody MenuItemRequest request) {
        return menuItemService.createItem(slug, request);
    }

    @PutMapping("/items/{id}")
    public MenuItem updateItem(@PathVariable String id, @RequestBody MenuItemRequest request) {
        return menuItemService.updateItem(id, request);
    }

    @PatchMapping("/items/{id}/availability")
    public MenuItem toggleAvailability(@PathVariable String id) {
        return menuItemService.toggleAvailability(id);
    }

    @PatchMapping("/categories/{slug}/items/reorder")
    public void reorderItems(@PathVariable String slug, @RequestBody List<String> orderedIds) {
        menuItemService.reorderItems(slug, orderedIds);
    }
}
