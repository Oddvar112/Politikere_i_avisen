package no.politikeriavisen.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"kandidat", "setninger"})
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

    @Column(name = "gir_sentiment", length = 10)
    private String girSentiment;

    @Column(name = "gir_positiv_score")
    private Double girPositivScore;

    @Column(name = "gir_negativ_score")
    private Double girNegativScore;

    @Column(name = "faar_sentiment", length = 10)
    private String faarSentiment;

    @Column(name = "faar_positiv_score")
    private Double faarPositivScore;

    @Column(name = "faar_negativ_score")
    private Double faarNegativScore;

    /**
     * Alle setninger som ble kjørt gjennom sentimentanalysen for denne
     * kandidat/artikkel-koblingen. Hver setning har en rolle (GIR/FAAR/UKJENT)
     * og individuelle scorer som tilsammen utgjør aggregatet over.
     */
    @OneToMany(mappedBy = "kandidatLink", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnalysertSetning> setninger = new ArrayList<>();

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

    /**
     * Legger til en analysert setning og holder den bidireksjonelle relasjonen
     * konsistent. Speiler {@code KandidatStortingsvalg.addLink()}.
     *
     * @param setning setning som skal knyttes til denne koblingen
     */
    public void addSetning(final AnalysertSetning setning) {
        if (setning != null) {
            this.setninger.add(setning);
            setning.setKandidatLink(this);
        }
    }
}