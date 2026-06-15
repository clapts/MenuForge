package it.menuforge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuDocument {
    @Builder.Default
    private String schemaVersion = MenuDocumentContract.SCHEMA_VERSION;
    private String instanceName;
    @Builder.Default
    private Instant updatedAt = Instant.now();
    @Builder.Default
    private List<Category> categories = new ArrayList<>();
    @Builder.Default
    private List<Badge> badges = new ArrayList<>();
    @Builder.Default
    private List<Allergen> allergens = new ArrayList<>();
}
