package it.menuforge.dto.response;

import it.menuforge.model.Category;
import lombok.Data;

import java.util.List;

@Data
public class CategoryResponse {
    private String id;
    private String slug;
    private String title;
    private String subtitle;
    private String note;
    private String categoryImageUrl;
    private boolean visible;
    private int position;
    private List<MenuItemResponse> items;

    public static CategoryResponse from(Category category) {
        return from(category, true);
    }

    public static CategoryResponse from(Category category, boolean includeItems) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setSlug(category.getSlug());
        response.setTitle(category.getTitle());
        response.setSubtitle(category.getSubtitle());
        response.setNote(category.getNote());
        response.setCategoryImageUrl(category.getCategoryImageUrl());
        response.setVisible(category.isVisible());
        response.setPosition(category.getPosition());
        if (includeItems) {
            response.setItems(category.getItems().stream()
                    .filter(item -> item.isAvailable())
                    .map(item -> MenuItemResponse.from(item, category))
                    .toList());
        }
        return response;
    }
}
