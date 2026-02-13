package folkestad.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO for å holde resultatet av et tekstsammendrag.
 * Inneholder link, sammendrag, kompresjonsrate, ordtelling og opprettelsesdato.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SammendragDTO {
    /** Link til artikkelen som er oppsummert. */
    private String link;
    /** Sammendragsteksten. */
    private String sammendrag;
    /** Kompresjonsrate (sammendrag/original). */
    private Double kompresjonRatio;
    /** Antall ord i originalteksten. */
    private Integer antallOrdOriginal;
    /** Antall ord i sammendraget. */
    private Integer antallOrdSammendrag;
    /** Dato sammendraget ble opprettet. */
    private LocalDateTime opprettetDato;
}
