package folkestad.project;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO som representerer én artikkel en person er nevnt i.
 * Inneholder lenke og dato artikkelen ble skrapet.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtikelDTO {
    /** URL til artikkelen. */
    private String lenke;
    /** Dato artikkelen ble skrapet. */
    private LocalDate scraped;
}


