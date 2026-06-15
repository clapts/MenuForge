package it.menuforge.dto.response;

import it.menuforge.model.Allergen;
import lombok.Data;

/**
 * Read-only response DTO for a single EU allergen.
 */
@Data
public class AllergenResponse {

    /** Official EU allergen number (1-14). */
    private Integer id;

    /** Machine-readable code (e.g., "GLUTEN", "MILK"). */
    private String code;

    /** Full Italian name per Regulation EU 1169/2011. */
    private String nameIt;

    public static AllergenResponse from(Allergen a) {
        AllergenResponse r = new AllergenResponse();
        r.setId(a.getId());
        r.setCode(a.getCode());
        r.setNameIt(a.getNameIt());
        return r;
    }
}
