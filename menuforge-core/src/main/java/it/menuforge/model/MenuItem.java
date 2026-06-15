package it.menuforge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {
    private String id;
    private String title;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String price;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String description;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String imageUrl;
    @Builder.Default
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean highlight = false;
    @Builder.Default
    private boolean available = true;
    @Builder.Default
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int position = 0;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String calories;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String origin;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String format;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String specialText1;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String specialText2;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String specialText3;
    @Builder.Default
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> ingredients = new ArrayList<>();
    @Builder.Default
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> tag1 = new ArrayList<>();
    @Builder.Default
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> tag2 = new ArrayList<>();
    @Builder.Default
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> tag3 = new ArrayList<>();
    @Builder.Default
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Badge> badges = new ArrayList<>();
    @Builder.Default
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Allergen> allergens = new ArrayList<>();
    @Builder.Default
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<CustomAttribute> customAttributes = new ArrayList<>();
}
