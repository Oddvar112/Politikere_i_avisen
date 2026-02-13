package folkestad;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "kandidat")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class KandidatLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(length = 1000)
    private String link;

    @Column(name = "scraped_at")
    private LocalDateTime scrapedAt;

    @Enumerated(EnumType.STRING)
    private Nettsted nettsted;

    @ManyToOne
    @JoinColumn(name = "kandidat_navn", nullable = false)
    private KandidatStortingsvalg kandidat;

    /**
     * Setter lenken og identifiserer automatisk nettsted basert på URL-en.
     *
     * @param link URL-en som skal lagres
     */
    public void setLinkAndDetectNettsted(final String link) {
        this.link = link;
        this.nettsted = Nettsted.parseFromUrl(link).orElse(null);
    }

    /**
     * Oppretter en KandidatLink med lenke og automatisk nettsted-identifisering.
     *
     * @param link     URL-en
     * @param kandidat Kandidaten som lenken tilhører
     * @return Ny KandidatLink med nettsted automatisk satt
     */
    public static KandidatLink createWithDetectedNettsted(final String link, final KandidatStortingsvalg kandidat) {
        KandidatLink kandidatLink = new KandidatLink();
        kandidatLink.setLinkAndDetectNettsted(link);
        kandidatLink.setKandidat(kandidat);
        kandidatLink.setScrapedAt(LocalDateTime.now());
        return kandidatLink;
    }
}
