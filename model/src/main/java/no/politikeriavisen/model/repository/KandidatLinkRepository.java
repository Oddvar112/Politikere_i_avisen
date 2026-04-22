package no.politikeriavisen.model.repository;

import no.politikeriavisen.model.entity.KandidatLink;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KandidatLinkRepository extends JpaRepository<KandidatLink, Long> {

    <S extends KandidatLink> List<S> saveAll(Iterable<S> entities);

    List<KandidatLink> findByLink(String link);

    @Query("SELECT kl FROM KandidatLink kl JOIN FETCH kl.kandidat WHERE kl.girSentiment IS NULL")
    List<KandidatLink> findWithoutSentiment();

    @Query(value = "SELECT ks.navn, "
            + "ks.partinavn, "
            + "ks.alder, "
            + "ks.kjoenn, "
            + "GROUP_CONCAT(DISTINCT ks.valgdistrikt ORDER BY ks.valgdistrikt SEPARATOR ',') as alle_valgdistrikt, "
            + "GROUP_CONCAT(kl.link ORDER BY kl.link SEPARATOR ',') as alle_lenker, "
            + "GROUP_CONCAT(DATE_FORMAT(kl.scraped_at, '%Y-%m-%d %H:%i:%s') ORDER BY kl.link SEPARATOR ',') as alle_scraped_at, "
            + "GROUP_CONCAT(IFNULL(kl.gir_sentiment, '') ORDER BY kl.link SEPARATOR ',') as alle_gir_sentiment, "
            + "GROUP_CONCAT(IFNULL(kl.gir_positiv_score, '') ORDER BY kl.link SEPARATOR ',') as alle_gir_positiv, "
            + "GROUP_CONCAT(IFNULL(kl.gir_negativ_score, '') ORDER BY kl.link SEPARATOR ',') as alle_gir_negativ, "
            + "GROUP_CONCAT(IFNULL(kl.faar_sentiment, '') ORDER BY kl.link SEPARATOR ',') as alle_faar_sentiment, "
            + "GROUP_CONCAT(IFNULL(kl.faar_positiv_score, '') ORDER BY kl.link SEPARATOR ',') as alle_faar_positiv, "
            + "GROUP_CONCAT(IFNULL(kl.faar_negativ_score, '') ORDER BY kl.link SEPARATOR ',') as alle_faar_negativ "
            + "FROM kandidat_link kl "
            + "JOIN kandidat_stortingsvalg ks ON kl.kandidat_navn = ks.navn "
            + "GROUP BY ks.navn, ks.partinavn, ks.alder, ks.kjoenn "
            + "ORDER BY ks.partinavn, ks.navn",
            nativeQuery = true)
    List<Object[]> findKandidatNavnWithLinks();

    @Query(value = "SELECT ks.navn, "
            + "ks.partinavn, "
            + "ks.alder, "
            + "ks.kjoenn, "
            + "GROUP_CONCAT(DISTINCT ks.valgdistrikt ORDER BY ks.valgdistrikt SEPARATOR ',') as alle_valgdistrikt, "
            + "GROUP_CONCAT(kl.link ORDER BY kl.link SEPARATOR ',') as alle_lenker, "
            + "GROUP_CONCAT(DATE_FORMAT(kl.scraped_at, '%Y-%m-%d %H:%i:%s') ORDER BY kl.link SEPARATOR ',') as alle_scraped_at, "
            + "GROUP_CONCAT(IFNULL(kl.gir_sentiment, '') ORDER BY kl.link SEPARATOR ',') as alle_gir_sentiment, "
            + "GROUP_CONCAT(IFNULL(kl.gir_positiv_score, '') ORDER BY kl.link SEPARATOR ',') as alle_gir_positiv, "
            + "GROUP_CONCAT(IFNULL(kl.gir_negativ_score, '') ORDER BY kl.link SEPARATOR ',') as alle_gir_negativ, "
            + "GROUP_CONCAT(IFNULL(kl.faar_sentiment, '') ORDER BY kl.link SEPARATOR ',') as alle_faar_sentiment, "
            + "GROUP_CONCAT(IFNULL(kl.faar_positiv_score, '') ORDER BY kl.link SEPARATOR ',') as alle_faar_positiv, "
            + "GROUP_CONCAT(IFNULL(kl.faar_negativ_score, '') ORDER BY kl.link SEPARATOR ',') as alle_faar_negativ "
            + "FROM kandidat_link kl "
            + "JOIN kandidat_stortingsvalg ks ON kl.kandidat_navn = ks.navn "
            + "WHERE kl.link LIKE CONCAT('%', :domain, '%') "
            + "GROUP BY ks.navn, ks.partinavn, ks.alder, ks.kjoenn "
            + "ORDER BY ks.partinavn, ks.navn",
            nativeQuery = true)
    List<Object[]> findKandidatNavnWithLinksByDomain(@Param("domain") String domain);
}
