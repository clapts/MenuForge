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
public class CustomAttribute {
    private String key;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String value;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String displayLabel;
}
