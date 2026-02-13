package folkestad;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import lombok.Builder;

/**
 * Entitet som representerer et lagret sammendrag av en artikkel.
 * Inneholder link, sammendrag, kompresjonsrate, ordtelling og opprettelsesdato.
 */
@Entity
@Table(name = "innlegg")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Innlegg {

    /** Unik ID for innlegget. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Link til artikkelen. */
    @Column(name = "link", length = 1000, nullable = false, unique = true)
    private String link;

    /** Sammendragsteksten. */
    @Lob
    @Column(name = "sammendrag", columnDefinition = "TEXT")
    private String sammendrag;

    /** Kompresjonsrate (sammendrag/original). */
    @Column(name = "kompresjon_ratio")
    private Double kompresjonRatio;

    /** Antall ord i originalteksten. */
    @Column(name = "antall_ord_original")
    private Integer antallOrdOriginal;

    /** Antall ord i sammendraget. */
    @Column(name = "antall_ord_sammendrag")
    private Integer antallOrdSammendrag;

    /** Dato innlegget ble opprettet. */
    @Column(name = "opprettet_dato")
    @Builder.Default
    private LocalDateTime opprettetDato = LocalDateTime.now();

    /**
     * Kalkulerer kompresjonsrate basert på antall ord i original og sammendrag.
     */
    public void calculateCompressionRatio() {
        if (antallOrdOriginal != null && antallOrdSammendrag != null && antallOrdOriginal > 0) {
            this.kompresjonRatio = (double) antallOrdSammendrag / antallOrdOriginal;
        }
    }

    /**
     * Setter sammendrag og oppdaterer statistikk (ordtelling og kompresjon).
     *
     * @param sammendrag    Sammendragstekst
     * @param originalTekst Original tekst
     */
    public void setSammendragWithStats(final String sammendrag, final String originalTekst) {
        this.sammendrag = sammendrag;
        if (sammendrag != null) {
            this.antallOrdSammendrag = sammendrag.split("\\s+").length;
        }
        if (originalTekst != null) {
            this.antallOrdOriginal = originalTekst.split("\\s+").length;
        }
        calculateCompressionRatio();
    }

}
