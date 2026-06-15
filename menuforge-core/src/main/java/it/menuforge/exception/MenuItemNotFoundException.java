package it.menuforge.exception;

public class MenuItemNotFoundException extends RuntimeException {

    public MenuItemNotFoundException(String id) {
        super("Menu item not found with id: " + id);
    }

    public MenuItemNotFoundException(Long id) {
        this(String.valueOf(id));
    }
}
