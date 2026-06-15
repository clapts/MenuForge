package it.menuforge.util;

import java.text.Normalizer;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class MenuForgeIds {

    private MenuForgeIds() {
    }

    public static String slugify(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return normalized.isBlank() ? "item" : normalized;
    }

    public static <T> String uniqueSlug(String base, Collection<T> existing, Function<T, String> extractor) {
        String candidateBase = slugify(base);
        Set<String> used = existing.stream()
                .map(extractor)
                .filter(v -> v != null && !v.isBlank())
                .collect(Collectors.toSet());
        String candidate = candidateBase;
        int suffix = 2;
        while (used.contains(candidate)) {
            candidate = candidateBase + "-" + suffix++;
        }
        return candidate;
    }
}
