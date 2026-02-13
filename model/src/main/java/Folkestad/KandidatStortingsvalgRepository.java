package folkestad;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KandidatStortingsvalgRepository extends JpaRepository<KandidatStortingsvalg, Long> {

    /**
     * Finn alle kandidater for et bestemt valgdistrikt.
     *
     * @param valgdistrikt valgdistriktet som skal søkes etter
     * @return liste med kandidater fra det spesifiserte valgdistriktet
     */
    List<KandidatStortingsvalg> findByValgdistrikt(String valgdistrikt);

    /**
     * Finn alle kandidater for et bestemt parti.
     *
     * @param partikode partikoden som skal søkes etter
     * @return liste med kandidater fra det spesifiserte partiet
     */
    List<KandidatStortingsvalg> findByPartikode(String partikode);

    /**
     * @return liste med alle kandidater med links
     */
    @Query("SELECT k FROM KandidatStortingsvalg k LEFT JOIN FETCH k.links")
    List<KandidatStortingsvalg> findAllWithLinks();

    /**
     * Finn alle kandidater for et bestemt parti og valgdistrikt.
     *
     * @param partikode    partikoden som skal søkes etter
     * @param valgdistrikt valgdistriktet som skal søkes etter
     * @return liste med kandidater fra det spesifiserte partiet og valgdistriktet
     */
    List<KandidatStortingsvalg> findByPartikodeAndValgdistrikt(String partikode, String valgdistrikt);

    /**
     * Søk etter kandidater basert på navn.
     *
     * @param navn navnet eller deler av navnet som skal søkes etter
     * @return liste med kandidater som matcher søket
     */
    @Query("SELECT k FROM KandidatStortingsvalg k WHERE LOWER(k.navn) LIKE LOWER(CONCAT('%', :navn, '%'))")
    List<KandidatStortingsvalg> findByNavnContainingIgnoreCase(@Param("navn") String navn);

    /**
     * Finn alle unike valgdistrikter.
     *
     * @return liste med alle unike valgdistrikter sortert alfabetisk
     */
    @Query("SELECT DISTINCT k.valgdistrikt FROM KandidatStortingsvalg k ORDER BY k.valgdistrikt")
    List<String> findAllDistinctValgdistrikter();

    /**
     * Finn alle unike partier.
     *
     * @return liste med alle unike partikoder sortert alfabetisk
     */
    @Query("SELECT DISTINCT k.partikode FROM KandidatStortingsvalg k ORDER BY k.partikode")
    List<String> findAllDistinctPartikoder();
}

