package it.menuforge.util;

import it.menuforge.model.Allergen;

import java.util.List;

public final class AllergenCatalog {

    private AllergenCatalog() {
    }

    public static List<Allergen> all() {
        return List.of(
                new Allergen(1, "GLUTEN", "Cereali contenenti glutine"),
                new Allergen(2, "CRUSTACEANS", "Crostacei e prodotti a base di crostacei"),
                new Allergen(3, "EGGS", "Uova e prodotti a base di uova"),
                new Allergen(4, "FISH", "Pesce e prodotti a base di pesce"),
                new Allergen(5, "PEANUTS", "Arachidi e prodotti a base di arachidi"),
                new Allergen(6, "SOYBEANS", "Soia e prodotti a base di soia"),
                new Allergen(7, "MILK", "Latte e prodotti a base di latte"),
                new Allergen(8, "NUTS", "Frutta a guscio"),
                new Allergen(9, "CELERY", "Sedano e prodotti a base di sedano"),
                new Allergen(10, "MUSTARD", "Senape e prodotti a base di senape"),
                new Allergen(11, "SESAME", "Semi di sesamo"),
                new Allergen(12, "SULPHITES", "Anidride solforosa e solfiti"),
                new Allergen(13, "LUPIN", "Lupini e prodotti a base di lupini"),
                new Allergen(14, "MOLLUSCS", "Molluschi e prodotti a base di molluschi")
        );
    }
}
