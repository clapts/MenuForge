package it.menuforge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

/**
 * Global exception handler for MenuForge REST controllers.
 *
 * <p>Returns RFC 9457 {@link ProblemDetail} responses for all MenuForge exceptions,
 * providing consistent error shapes to API consumers.
 */
@RestControllerAdvice(basePackages = "it.menuforge.controller")
public class MenuForgeExceptionHandler {

    @ExceptionHandler(CategoryNotFoundException.class)
    public ProblemDetail handleCategoryNotFound(CategoryNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(URI.create("https://menuforge.it/errors/category-not-found"));
        pd.setTitle("Category Not Found");
        return pd;
    }

    @ExceptionHandler(MenuItemNotFoundException.class)
    public ProblemDetail handleMenuItemNotFound(MenuItemNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(URI.create("https://menuforge.it/errors/item-not-found"));
        pd.setTitle("Menu Item Not Found");
        return pd;
    }

    @ExceptionHandler(BadgeNotFoundException.class)
    public ProblemDetail handleBadgeNotFound(BadgeNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(URI.create("https://menuforge.it/errors/badge-not-found"));
        pd.setTitle("Badge Not Found");
        return pd;
    }

    @ExceptionHandler(DuplicateMenuResourceException.class)
    public ProblemDetail handleDuplicateResource(DuplicateMenuResourceException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setType(URI.create("https://menuforge.it/errors/duplicate-resource"));
        pd.setTitle("Duplicate Menu Resource");
        return pd;
    }

    @ExceptionHandler(InvalidMenuDocumentException.class)
    public ProblemDetail handleInvalidMenuDocument(InvalidMenuDocumentException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setType(URI.create("https://menuforge.it/errors/invalid-menu-document"));
        pd.setTitle("Invalid Menu Document");
        return pd;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setType(URI.create("https://menuforge.it/errors/bad-request"));
        pd.setTitle("Bad Request");
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        pd.setType(URI.create("https://menuforge.it/errors/validation-failed"));
        pd.setTitle("Validation Failed");
        return pd;
    }
}
