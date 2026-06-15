package it.menuforge.exception;

public class BadgeNotFoundException extends RuntimeException {

    public BadgeNotFoundException(String id) {
        super("Badge not found with id: " + id);
    }
}
