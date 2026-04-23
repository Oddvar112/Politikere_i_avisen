package no.politikeriavisen.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import lombok.Builder;
import org.springframework.data.domain.Persistable;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "kandidat_stortingsvalg")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class KandidatStortingsvalg implements Persistable<String> {

    @Id
    @Column(name = "navn")
    @EqualsAndHashCode.Include
    private String navn;

    @Column(name = "valg")
    private String valg;

    @Column(name = "valgdistrikt")
    private String valgdistrikt;

    @Column(name = "partikode")
    private String partikode;

    @Column(name = "partinavn")
    private String partinavn;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "kandidatnr")
    private Integer kandidatnr;

    @Column(name = "bosted")
    private String bosted;

    @Column(name = "stilling")
    private String stilling;

    @Column(name = "foedselsdato")
    private LocalDate foedselsdato;

    @Column(name = "alder")
    private Integer alder;

    @Column(name = "kjoenn")
    private String kjoenn;

    @OneToMany(mappedBy = "kandidat", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<KandidatLink> links = new HashSet<>();

    /**
     * Transient flagg som brukes av Spring Data til å avgjøre om
     * entiteten er ny (persist) eller eksisterende (merge). Siden vi
     * har naturlig nøkkel ({@code navn}), ville default-logikken ellers
     * alltid kalt merge — som kolliderer med {@code orphanRemoval = true}
     * på {@code links}-kolleksjonen.
     *
     * <p>Settes til {@code false} automatisk etter at entiteten er lastet
     * fra DB ({@link PostLoad}) eller nylig persistert ({@link PostPersist}).
     */
    @Transient
    @Builder.Default
    @ToString.Exclude
    private boolean nyKandidat = true;

    @PostLoad
    @PostPersist
    void markLagret() {
        this.nyKandidat = false;
    }

    @Override
    @Transient
    public String getId() {
        return navn;
    }

    @Override
    @Transient
    public boolean isNew() {
        return nyKandidat;
    }

    /**
     * Legger til en KandidatLink og sikrer bidireksjonell synkronisering.
     *
     * @param kandidatLink KandidatLink som skal legges til
     */
    public void addLink(final KandidatLink kandidatLink) {
        if (kandidatLink != null) {
            this.links.add(kandidatLink);
            kandidatLink.setKandidat(this);
        }
    }

    /**
     * Fjerner en KandidatLink og sikrer bidireksjonell synkronisering.
     *
     * @param kandidatLink KandidatLink som skal fjernes
     */
    public void removeLink(final KandidatLink kandidatLink) {
        if (kandidatLink != null) {
            this.links.remove(kandidatLink);
            kandidatLink.setKandidat(null);
        }
    }
}
