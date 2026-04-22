package no.politikeriavisen.core.analysis;

import no.politikeriavisen.model.repository.KandidatLinkRepository;
import no.politikeriavisen.dto.DataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

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

    public DataDTO getDataForKilde(final String kilde) {
        String normalizedKilde = kilde.toLowerCase().trim();
        String domain = KILDE_DOMAIN_MAP.get(normalizedKilde);
        if (domain == null) {
            throw new IllegalArgumentException("Ukjent kilde: " + kilde);
        }

        List<Object[]> rawData = "ALT".equals(domain)
                ? kandidatLinkRepository.findKandidatNavnWithLinks()
                : kandidatLinkRepository.findKandidatNavnWithLinksByDomain(domain);
        return kildeDataAnalyzer.analyzeDataByKilde(rawData, domain);
    }
}
