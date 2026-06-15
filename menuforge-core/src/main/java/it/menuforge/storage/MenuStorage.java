package it.menuforge.storage;

import it.menuforge.model.MenuDocument;

public interface MenuStorage {
    MenuDocument load();

    MenuDocument save(MenuDocument document);
}
