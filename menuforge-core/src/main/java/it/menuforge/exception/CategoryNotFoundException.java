package it.menuforge.exception;

/**
 * Thrown when a {@link it.menuforge.model.Category} with the requested slug
 * or ID does not exist in the menu document.
 */
public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(String slug) {
        super("Category not found: '" + slug + "'");
    }

    public CategoryNotFoundException(Long id) {
        super("Category not found with id: " + id);
    }
}
