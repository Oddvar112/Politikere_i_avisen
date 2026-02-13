package folkestad;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import lombok.Builder;

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
public class KandidatStortingsvalg {

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

