package no.politikeriavisen.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * En enkelt setning fra en artikkel som er blitt kjørt gjennom
 * sentimentanalysen for en bestemt politiker. Lagres for å gjøre
 * aggregat-scoren på {@link KandidatLink} etterprøvbar — man kan se
 * nøyaktig hvilke setninger som bidro, rollene deres (GIR/FAAR/UKJENT),
 * og individuelle NorBERT-scorer.
 */
@Entity
@Table(
    name = "analysert_setning",
    indexes = @Index(name = "idx_analysert_setning_link", columnList = "kandidat_link_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "kandidatLink")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AnalysertSetning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kandidat_link_id", nullable = false)
    private KandidatLink kandidatLink;

    @Column(name = "tekst", columnDefinition = "TEXT", nullable = false)
    private String tekst;

    @Column(name = "posisjon")
    private Integer posisjon;

    @Column(name = "matchet_navn", length = 255)
    private String matchetNavn;

    @Enumerated(EnumType.STRING)
    @Column(name = "rolle", length = 16, nullable = false)
    private SetningRolle rolle;

    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment", length = 16, nullable = false)
    private Sentiment sentiment;

    @Column(name = "positiv_score")
    private Double positivScore;

    @Column(name = "noytral_score")
    private Double noytralScore;

    @Column(name = "negativ_score")
    private Double negativScore;

    @Column(name = "analysert_at", nullable = false)
    private LocalDateTime analysertAt;

    @PrePersist
    void settAnalysertAtHvisNull() {
        if (this.analysertAt == null) {
            this.analysertAt = LocalDateTime.now();
        }
    }
}
