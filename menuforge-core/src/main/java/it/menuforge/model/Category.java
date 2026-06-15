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
public class Category {
    private String id;
    private String slug;
    private String title;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String subtitle;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String note;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String categoryImageUrl;
    @Builder.Default
    private boolean visible = true;
    @Builder.Default
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int position = 0;
    @Builder.Default
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<MenuItem> items = new ArrayList<>();
}
