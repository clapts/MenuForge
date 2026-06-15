package it.menuforge.service;

import it.menuforge.model.Allergen;
import it.menuforge.storage.MenuStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AllergenService {

    private final MenuStorage storage;

    public List<Allergen> getAllAllergens() {
        return storage.load().getAllergens().stream()
                .sorted(Comparator.comparing(Allergen::getId))
                .toList();
    }

    public Optional<Allergen> getAllergenById(int number) {
        return getAllAllergens().stream()
                .filter(allergen -> allergen.getId() == number)
                .findFirst();
    }

    public Optional<Allergen> getAllergenByCode(String code) {
        if (code == null) {
            return Optional.empty();
        }
        return getAllAllergens().stream()
                .filter(allergen -> code.equalsIgnoreCase(allergen.getCode()))
                .findFirst();
    }
}
