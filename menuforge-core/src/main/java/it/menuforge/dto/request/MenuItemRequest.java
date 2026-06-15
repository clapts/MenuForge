package it.menuforge.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class MenuItemRequest {
    private String id;
    private String title;
    private String price;
    private String description;
    private String imageUrl;
    private Boolean highlight;
    private Boolean available;
    private Integer position;
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
    private List<Integer> allergenNumbers;
    private List<String> badgeIds;
}
