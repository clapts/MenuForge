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
public class Category {
    private String id;
    private String slug;
    private String title;
    private String subtitle;
    private String note;
    private String categoryImageUrl;
    @Builder.Default
    private boolean visible = true;
    @Builder.Default
    private int position = 0;
    @Builder.Default
    private List<MenuItem> items = new ArrayList<>();
}
