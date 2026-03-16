package no.politikeriavisen.core.analysis;

import no.politikeriavisen.model.repository.KandidatLinkRepository;
import no.politikeriavisen.dto.DataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

/**
 * Service for å håndtere kandidat analyse med caching.
 * Bruker Spring Cache for å lagre analysedata per kilde.
 */
@Service
public class KandidateAnalysis {

    private static final Map<String, String> KILDE_DOMAIN_MAP = Map.of(
            "vg", "vg.no",
            "nrk", "nrk.no",
            "e24", "e24.no",
            "dagbladet", "dagbladet.no",
            "alt", "ALT",
            "all", "ALT"
    );

    @Autowired
    private KandidatLinkRepository kandidatLinkRepository;

    @Autowired
    private KildeDataAnalyzer kildeDataAnalyzer;

    /**
     * Henter analysedata for en gitt kilde. Resultatet caches automatisk.
     *
     * @param kilde Kilde å hente data for ("vg", "nrk", "e24", "dagbladet", "alt", "all")
     * @return DataDTO med analysedata for kilden
     * @throws IllegalArgumentException hvis ukjent kilde
     */
    @Cacheable("analyseData")
    public DataDTO getDataForKilde(final String kilde) {
        String normalizedKilde = kilde.toLowerCase().trim();
        String domain = KILDE_DOMAIN_MAP.get(normalizedKilde);
        if (domain == null) {
            throw new IllegalArgumentException("Ukjent kilde: " + kilde);
        }

        List<Object[]> rawData = kandidatLinkRepository.findKandidatNavnWithLinks();
        return kildeDataAnalyzer.analyzeDataByKilde(rawData, domain);
    }

    /**
     * Tømmer cachen slik at neste kall til getDataForKilde henter ferske data.
     */
    @CacheEvict(value = "analyseData", allEntries = true)
    public void oppdater() {
        // Cache tømmes automatisk av @CacheEvict
    }
}
