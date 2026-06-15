package it.menuforge.dto.request;

import lombok.Data;

@Data
public class CategoryRequest {
    private String slug;
    private String title;
    private String subtitle;
    private String note;
    private String categoryImageUrl;
    private Boolean visible;
    private Integer position;
}
