package it.menuforge.dto.response;

import it.menuforge.model.Category;
import it.menuforge.model.MenuItem;
import lombok.Data;

import java.util.List;

@Data
public class MenuItemResponse {
    private String id;
    private String categoryId;
    private String categorySlug;
    private String title;
    private String price;
    private String description;
    private String imageUrl;
    private boolean highlight;
    private boolean available;
    private int position;
    private String calories;
    private String origin;
    private String format;
    private String specialText1;
    private String specialText2;
    private String specialText3;
    private List<String> ingredients;
    private List<String> tag1;
    private List<String> tag2;
    private List<String> tag3;
    private List<BadgeInfo> badges;
    private List<AllergenResponse> allergens;
    private List<CustomAttributeInfo> customAttributes;

    @Data
    public static class BadgeInfo {
        private String id;
        private String label;
        private String style;
    }

    @Data
    public static class CustomAttributeInfo {
        private String key;
        private String value;
        private String displayLabel;
    }

    public static MenuItemResponse from(MenuItem item) {
        return from(item, null);
    }

    public static MenuItemResponse from(MenuItem item, Category category) {
        MenuItemResponse response = new MenuItemResponse();
        response.setId(item.getId());
        if (category != null) {
            response.setCategoryId(category.getId());
            response.setCategorySlug(category.getSlug());
        }
        response.setTitle(item.getTitle());
        response.setPrice(item.getPrice());
        response.setDescription(item.getDescription());
        response.setImageUrl(item.getImageUrl());
        response.setHighlight(item.isHighlight());
        response.setAvailable(item.isAvailable());
        response.setPosition(item.getPosition());
        response.setCalories(item.getCalories());
        response.setOrigin(item.getOrigin());
        response.setFormat(item.getFormat());
        response.setSpecialText1(item.getSpecialText1());
        response.setSpecialText2(item.getSpecialText2());
        response.setSpecialText3(item.getSpecialText3());
        response.setIngredients(item.getIngredients());
        response.setTag1(item.getTag1());
        response.setTag2(item.getTag2());
        response.setTag3(item.getTag3());
        response.setBadges(item.getBadges().stream().map(badge -> {
            BadgeInfo info = new BadgeInfo();
            info.setId(badge.getId());
            info.setLabel(badge.getLabel());
            info.setStyle(badge.getStyle());
            return info;
        }).toList());
        response.setAllergens(item.getAllergens().stream().map(AllergenResponse::from).toList());
        response.setCustomAttributes(item.getCustomAttributes().stream().map(attribute -> {
            CustomAttributeInfo info = new CustomAttributeInfo();
            info.setKey(attribute.getKey());
            info.setValue(attribute.getValue());
            info.setDisplayLabel(attribute.getDisplayLabel());
            return info;
        }).toList());
        return response;
    }
}
