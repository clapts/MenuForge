package it.menuforge.validation;

import it.menuforge.model.*;
import it.menuforge.exception.DuplicateMenuResourceException;
import it.menuforge.exception.InvalidMenuDocumentException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class MenuDocumentValidator {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    private MenuDocumentValidator() {
    }

    public static void validate(MenuDocument document) {
        if (document == null) {
            throw invalid("Menu document is required");
        }
        if (!MenuDocumentContract.SCHEMA_VERSION.equals(document.getSchemaVersion())) {
            throw invalid("Unsupported menu schemaVersion: " + document.getSchemaVersion());
        }
        requireList(document.getCategories(), "categories");
        requireList(document.getBadges(), "badges");
        requireList(document.getAllergens(), "allergens");

        validateAllergens(document.getAllergens());
        validateBadges(document.getBadges());
        validateCategories(document.getCategories());
    }

    private static void validateCategories(List<Category> categories) {
        Set<String> categorySlugs = new HashSet<>();
        Set<String> itemIds = new HashSet<>();
        for (Category category : categories) {
            requireText(category.getSlug(), "Category slug is required");
            requireSlug(category.getSlug(), "Category slug");
            requireText(category.getTitle(), "Category title is required for slug '" + category.getSlug() + "'");
            if (!categorySlugs.add(category.getSlug())) {
                throw duplicate("Duplicate category slug: " + category.getSlug());
            }
            if (category.getId() != null && !category.getId().isBlank() && !category.getId().equals(category.getSlug())) {
                throw invalid("Category id must match slug for category '" + category.getSlug() + "'");
            }
            if (category.getPosition() < 0) {
                throw invalid("Category position cannot be negative: " + category.getSlug());
            }
            requireList(category.getItems(), "items for category '" + category.getSlug() + "'");
            validateItems(category, itemIds);
        }
    }

    private static void validateItems(Category category, Set<String> itemIds) {
        for (MenuItem item : category.getItems()) {
            requireText(item.getId(), "Item id is required in category '" + category.getSlug() + "'");
            requireSlug(item.getId(), "Item id");
            requireText(item.getTitle(), "Item title is required for id '" + item.getId() + "'");
            if (!itemIds.add(item.getId())) {
                throw duplicate("Duplicate item id across menu: " + item.getId());
            }
            if (item.getPosition() < 0) {
                throw invalid("Item position cannot be negative: " + item.getId());
            }
            validateStringList(item.getIngredients(), "ingredients", item.getId(), false);
            validateStringList(item.getTag1(), "tag1", item.getId(), true);
            validateStringList(item.getTag2(), "tag2", item.getId(), true);
            validateStringList(item.getTag3(), "tag3", item.getId(), true);
            validateBadges(item.getBadges(), "item '" + item.getId() + "'");
            validateAllergens(item.getAllergens(), "item '" + item.getId() + "'");
            validateCustomAttributes(item.getCustomAttributes(), item.getId());
        }
    }

    private static void validateBadges(List<Badge> badges) {
        validateBadges(badges, "document");
        Set<String> ids = new HashSet<>();
        for (Badge badge : badges) {
            if (!ids.add(badge.getId())) {
                throw duplicate("Duplicate badge id: " + badge.getId());
            }
        }
    }

    private static void validateBadges(List<Badge> badges, String scope) {
        requireList(badges, "badges for " + scope);
        for (Badge badge : badges) {
            if (badge == null) {
                throw invalid("Badge cannot be null in " + scope);
            }
            requireText(badge.getId(), "Badge id is required in " + scope);
            requireSlug(badge.getId(), "Badge id");
            requireText(badge.getLabel(), "Badge label is required for badge '" + badge.getId() + "'");
        }
    }

    private static void validateAllergens(List<Allergen> allergens) {
        validateAllergens(allergens, "document");
        Set<Integer> ids = new HashSet<>();
        for (Allergen allergen : allergens) {
            if (!ids.add(allergen.getId())) {
                throw duplicate("Duplicate allergen id: " + allergen.getId());
            }
        }
    }

    private static void validateAllergens(List<Allergen> allergens, String scope) {
        requireList(allergens, "allergens for " + scope);
        for (Allergen allergen : allergens) {
            if (allergen == null) {
                throw invalid("Allergen cannot be null in " + scope);
            }
            if (allergen.getId() == null || allergen.getId() < 1 || allergen.getId() > 14) {
                throw invalid("Invalid allergen id in " + scope + ": " + allergen.getId());
            }
            requireText(allergen.getCode(), "Allergen code is required for id " + allergen.getId());
            requireText(allergen.getNameIt(), "Allergen nameIt is required for id " + allergen.getId());
        }
    }

    private static void validateCustomAttributes(List<CustomAttribute> attributes, String itemId) {
        requireList(attributes, "customAttributes for item '" + itemId + "'");
        Set<String> keys = new HashSet<>();
        for (CustomAttribute attribute : attributes) {
            if (attribute == null) {
                throw invalid("Custom attribute cannot be null for item '" + itemId + "'");
            }
            requireText(attribute.getKey(), "Custom attribute key is required for item '" + itemId + "'");
            if (!keys.add(attribute.getKey())) {
                throw duplicate("Duplicate custom attribute key '" + attribute.getKey() + "' for item '" + itemId + "'");
            }
        }
    }

    private static void validateStringList(List<String> values, String field, String itemId, boolean requireSlugValues) {
        requireList(values, field + " for item '" + itemId + "'");
        Set<String> seen = new HashSet<>();
        for (String value : values) {
            requireText(value, field + " cannot contain blank values for item '" + itemId + "'");
            if (requireSlugValues) {
                requireSlug(value, field + " value");
            }
            if (!seen.add(value)) {
                throw duplicate("Duplicate " + field + " value '" + value + "' for item '" + itemId + "'");
            }
        }
    }

    private static void requireList(List<?> values, String field) {
        if (values == null) {
            throw invalid(field + " cannot be null");
        }
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw invalid(message);
        }
    }

    private static void requireSlug(String value, String field) {
        if (!SLUG_PATTERN.matcher(value).matches()) {
            throw invalid(field + " must contain lowercase letters, digits and hyphens only: " + value);
        }
    }

    private static InvalidMenuDocumentException invalid(String message) {
        return new InvalidMenuDocumentException(message);
    }

    private static DuplicateMenuResourceException duplicate(String message) {
        return new DuplicateMenuResourceException(message);
    }
}
