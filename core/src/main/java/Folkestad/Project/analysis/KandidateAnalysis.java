package folkestad.project.analysis;

import folkestad.KandidatLinkRepository;
import folkestad.project.DataDTO;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for å håndtere kandidat analyse med caching.
 * Holder analysedata for VG, NRK, E24 og samlet (ALT)
 */
@Service
@Getter
public class KandidateAnalysis {

    @Autowired
    private KandidatLinkRepository kandidatLinkRepository;

    private DataDTO dataVG;
    private DataDTO dataNRK;
    private DataDTO dataE24;
    private DataDTO dataAlt;
    private DataDTO dataDagbladet;

    private LocalDateTime sistOppdatert;

    /**
     * Utfører kandidat-analyse og oppdaterer cache for alle kilder.
     * Henter data fra database og filtrerer per kilde.
     *
     * Ingen parametre.
     * Setter analysedata for VG, NRK, E24, Dagbladet og ALT.
     */
    public void analyzeKandidatData() {
        List<Object[]> rawData = kandidatLinkRepository.findKandidatNavnWithLinks();

        dataVG = KildeDataAnalyzer.analyzeDataByKilde(rawData, "vg.no");
        dataNRK = KildeDataAnalyzer.analyzeDataByKilde(rawData, "nrk.no");
        dataE24 = KildeDataAnalyzer.analyzeDataByKilde(rawData, "e24.no");
        dataAlt = KildeDataAnalyzer.analyzeDataByKilde(rawData, "ALT");
        dataDagbladet = KildeDataAnalyzer.analyzeDataByKilde(rawData, "dagbladet.no");

        sistOppdatert = LocalDateTime.now();
    }

    /**
     * Oppdaterer analyse-dataene fra database ved å kjøre analyse på nytt.
     *
     * Ingen parametre.
     * Ingen returverdi.
     */
    public void oppdater() {
        analyzeKandidatData();
    }

    /**
     * Sjekker om analysedata er tilgjengelig.
     *
     * @return true hvis analyse er kjørt og dataAlt er satt, ellers false
     */
    public boolean erDataTilgjengelig() {
        return dataAlt != null;
    }
}

