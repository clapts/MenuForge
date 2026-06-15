package it.menuforge.service;

import it.menuforge.dto.request.CategoryRequest;
import it.menuforge.exception.CategoryNotFoundException;
import it.menuforge.exception.DuplicateMenuResourceException;
import it.menuforge.exception.InvalidMenuDocumentException;
import it.menuforge.model.Category;
import it.menuforge.model.MenuDocument;
import it.menuforge.storage.MenuStorage;
import it.menuforge.util.MenuForgeIds;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final MenuStorage storage;

    public List<Category> getAllCategories() {
        return storage.load().getCategories().stream()
                .sorted(Comparator.comparing(Category::getPosition))
                .toList();
    }

    public List<Category> getVisibleCategories() {
        return getAllCategories().stream()
                .filter(Category::isVisible)
                .toList();
    }

    public Optional<Category> getCategoryBySlug(String slug) {
        return storage.load().getCategories().stream()
                .filter(category -> category.getSlug().equals(slug))
                .findFirst();
    }

    public Category getCategoryBySlugOrThrow(String slug) {
        return getCategoryBySlug(slug).orElseThrow(() -> new CategoryNotFoundException(slug));
    }

    public Category createCategory(CategoryRequest request) {
        requireText(request.getTitle(), "Category title is required");
        MenuDocument document = storage.load();
        String slug = request.getSlug();
        if (slug == null || slug.isBlank()) {
            slug = MenuForgeIds.uniqueSlug(request.getTitle(), document.getCategories(), Category::getSlug);
        } else {
            slug = MenuForgeIds.slugify(slug);
        }
        ensureSlugAvailable(document, slug, null);

        Category category = Category.builder()
                .id(slug)
                .slug(slug)
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
                .note(request.getNote())
                .categoryImageUrl(request.getCategoryImageUrl())
                .visible(request.getVisible() == null || request.getVisible())
                .position(request.getPosition() != null ? request.getPosition() : document.getCategories().size())
                .build();
        document.getCategories().add(category);
        storage.save(document);
        return category;
    }

    public Category updateCategory(String slug, CategoryRequest request) {
        MenuDocument document = storage.load();
        Category category = findCategory(document, slug);
        String newSlug = request.getSlug() == null || request.getSlug().isBlank()
                ? category.getSlug()
                : MenuForgeIds.slugify(request.getSlug());
        ensureSlugAvailable(document, newSlug, category.getSlug());

        category.setSlug(newSlug);
        category.setId(newSlug);
        if (request.getTitle() != null) category.setTitle(request.getTitle());
        if (request.getSubtitle() != null) category.setSubtitle(request.getSubtitle());
        if (request.getNote() != null) category.setNote(request.getNote());
        if (request.getCategoryImageUrl() != null) category.setCategoryImageUrl(request.getCategoryImageUrl());
        if (request.getVisible() != null) category.setVisible(request.getVisible());
        if (request.getPosition() != null) category.setPosition(request.getPosition());
        storage.save(document);
        return category;
    }

    public void deleteCategory(String slug) {
        MenuDocument document = storage.load();
        boolean removed = document.getCategories().removeIf(category -> category.getSlug().equals(slug));
        if (!removed) {
            throw new CategoryNotFoundException(slug);
        }
        storage.save(document);
    }

    public Category toggleVisibility(String slug) {
        MenuDocument document = storage.load();
        Category category = findCategory(document, slug);
        category.setVisible(!category.isVisible());
        storage.save(document);
        return category;
    }

    public void reorderCategories(List<String> orderedSlugs) {
        MenuDocument document = storage.load();
        AtomicInteger position = new AtomicInteger(0);
        for (String slug : orderedSlugs) {
            findCategory(document, slug).setPosition(position.getAndIncrement());
        }
        storage.save(document);
    }

    public Category replaceCategory(String slug, Category replacement) {
        MenuDocument document = storage.load();
        int index = -1;
        for (int i = 0; i < document.getCategories().size(); i++) {
            if (document.getCategories().get(i).getSlug().equals(slug)) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            throw new CategoryNotFoundException(slug);
        }
        normalizeCategory(replacement, slug, index);
        document.getCategories().set(index, replacement);
        storage.save(document);
        return replacement;
    }

    private Category findCategory(MenuDocument document, String slug) {
        return document.getCategories().stream()
                .filter(category -> category.getSlug().equals(slug))
                .findFirst()
                .orElseThrow(() -> new CategoryNotFoundException(slug));
    }

    private void ensureSlugAvailable(MenuDocument document, String slug, String currentSlug) {
        boolean exists = document.getCategories().stream()
                .anyMatch(category -> category.getSlug().equals(slug) && !category.getSlug().equals(currentSlug));
        if (exists) {
            throw new DuplicateMenuResourceException("A category with slug '" + slug + "' already exists");
        }
    }

    private void normalizeCategory(Category category, String fallbackSlug, int fallbackPosition) {
        String slug = category.getSlug() == null || category.getSlug().isBlank()
                ? fallbackSlug
                : MenuForgeIds.slugify(category.getSlug());
        category.setSlug(slug);
        category.setId(slug);
        if (category.getItems() == null) {
            category.setItems(new java.util.ArrayList<>());
        }
        if (category.getPosition() < 0) {
            category.setPosition(fallbackPosition);
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidMenuDocumentException(message);
        }
    }
}
