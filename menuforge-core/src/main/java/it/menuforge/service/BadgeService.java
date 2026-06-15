package it.menuforge.service;

import it.menuforge.dto.request.BadgeRequest;
import it.menuforge.exception.BadgeNotFoundException;
import it.menuforge.exception.DuplicateMenuResourceException;
import it.menuforge.exception.InvalidMenuDocumentException;
import it.menuforge.model.Badge;
import it.menuforge.model.MenuDocument;
import it.menuforge.storage.MenuStorage;
import it.menuforge.util.MenuForgeIds;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final MenuStorage storage;

    public List<Badge> getAllBadges() {
        return storage.load().getBadges();
    }

    public Optional<Badge> getBadgeById(String id) {
        return getAllBadges().stream()
                .filter(badge -> badge.getId().equals(id))
                .findFirst();
    }

    public Optional<Badge> getBadgeByLabel(String label) {
        return getAllBadges().stream()
                .filter(badge -> badge.getLabel().equals(label))
                .findFirst();
    }

    public Badge createBadge(BadgeRequest request) {
        requireText(request.getLabel(), "Badge label is required");
        MenuDocument document = storage.load();
        String id = request.getId() == null || request.getId().isBlank()
                ? MenuForgeIds.uniqueSlug(request.getLabel(), document.getBadges(), Badge::getId)
                : MenuForgeIds.slugify(request.getId());
        if (document.getBadges().stream().anyMatch(badge -> badge.getId().equals(id))) {
            throw new DuplicateMenuResourceException("A badge with id '" + id + "' already exists");
        }
        Badge badge = new Badge(id, request.getLabel(), request.getStyle());
        document.getBadges().add(badge);
        storage.save(document);
        return badge;
    }

    public Badge updateBadge(String id, BadgeRequest request) {
        MenuDocument document = storage.load();
        Badge badge = document.getBadges().stream()
                .filter(candidate -> candidate.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new BadgeNotFoundException(id));
        if (request.getLabel() != null) badge.setLabel(request.getLabel());
        if (request.getStyle() != null) badge.setStyle(request.getStyle());
        storage.save(document);
        return badge;
    }

    public void deleteBadge(String id) {
        MenuDocument document = storage.load();
        boolean removed = document.getBadges().removeIf(badge -> badge.getId().equals(id));
        if (!removed) {
            throw new BadgeNotFoundException(id);
        }
        document.getCategories().forEach(category ->
                category.getItems().forEach(item ->
                        item.getBadges().removeIf(badge -> badge.getId().equals(id))));
        storage.save(document);
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidMenuDocumentException(message);
        }
    }
}
