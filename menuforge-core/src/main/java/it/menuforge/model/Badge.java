package it.menuforge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Badge {
    private String id;
    private String label;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String style;
}
