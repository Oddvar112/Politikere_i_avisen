package no.politikeriavisen.model.repository;

import no.politikeriavisen.model.entity.AnalysertSetning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link AnalysertSetning}. Setningene opprettes vanligvis
 * via cascade fra {@link no.politikeriavisen.model.entity.KandidatLink},
 * men eksplisitte spørringer eksponeres her for forklaringsvisning i
 * front-end og for eventuelle vedlikeholdsoppgaver.
 */
@Repository
public interface AnalysertSetningRepository extends JpaRepository<AnalysertSetning, Long> {

    /**
     * Alle setninger knyttet til en gitt KandidatLink, sortert etter posisjon.
     *
     * @param kandidatLinkId id til koblingen
     * @return setninger i rekkefølge de forekommer i artikkelen
     */
    List<AnalysertSetning> findByKandidatLink_IdOrderByPosisjonAsc(Long kandidatLinkId);
}
