package it.menuforge.dto.response;

import lombok.Data;

import java.util.List;

/**
 * Top-level response returned by {@code GET /api/menu}.
 *
 * <p>Contains the full menu: instance name and all visible categories with their items.
 */
@Data
public class MenuResponse {

    /** Human-readable name of this menu instance (from {@code menuforge.instance-name}). */
    private String instanceName;

    /** All categories (ordered by position), each containing their items. */
    private List<CategoryResponse> categories;

    public static MenuResponse of(String instanceName, List<CategoryResponse> categories) {
        MenuResponse r = new MenuResponse();
        r.setInstanceName(instanceName);
        r.setCategories(categories);
        return r;
    }
}
