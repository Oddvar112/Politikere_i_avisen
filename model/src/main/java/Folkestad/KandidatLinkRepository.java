package folkestad;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface KandidatLinkRepository extends JpaRepository<KandidatLink, Long> {

    /**
     * SQL-basert gruppering som returnerer alle lenker per kandidat.
     * Returnerer én rad per kandidat med navn, parti, alder, kjønn,
     * valgdistrikt(er) og alle lenker som kommaseparerte strenger.
     *
     * @return Liste med kandidatnavn, parti, alder, kjønn, valgdistrikt(er) og
     *         deres samlede lenker
     */
    @Query(value = "SELECT ks.navn, "
            + "ks.partinavn, "
            + "ks.alder, "
            + "ks.kjoenn, "
            + "GROUP_CONCAT(DISTINCT ks.valgdistrikt ORDER BY ks.valgdistrikt SEPARATOR ',') as alle_valgdistrikt, "
            + "GROUP_CONCAT(kl.link ORDER BY kl.link SEPARATOR ',') as alle_lenker, "
            + "GROUP_CONCAT(DATE_FORMAT(kl.scraped_at, '%Y-%m-%d %H:%i:%s') ORDER BY kl.link SEPARATOR ',') as alle_scraped_at "
            + "FROM kandidat_link kl "
            + "JOIN kandidat_stortingsvalg ks ON kl.kandidat_navn = ks.navn "
            + "GROUP BY ks.navn, ks.partinavn, ks.alder, ks.kjoenn "
            + "ORDER BY ks.partinavn, ks.navn",
            nativeQuery = true)
    List<Object[]> findKandidatNavnWithLinks();
}

