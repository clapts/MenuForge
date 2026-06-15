package it.menuforge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {
    private String id;
    private String title;
    private String price;
    private String description;
    private String imageUrl;
    @Builder.Default
    private boolean highlight = false;
    @Builder.Default
    private boolean available = true;
    @Builder.Default
    private int position = 0;
    private String calories;
    private String origin;
    private String format;
    private String specialText1;
    private String specialText2;
    private String specialText3;
    @Builder.Default
    private List<String> ingredients = new ArrayList<>();
    @Builder.Default
    private List<String> tag1 = new ArrayList<>();
    @Builder.Default
    private List<String> tag2 = new ArrayList<>();
    @Builder.Default
    private List<String> tag3 = new ArrayList<>();
    @Builder.Default
    private List<Badge> badges = new ArrayList<>();
    @Builder.Default
    private List<Allergen> allergens = new ArrayList<>();
    @Builder.Default
    private List<CustomAttribute> customAttributes = new ArrayList<>();
}
