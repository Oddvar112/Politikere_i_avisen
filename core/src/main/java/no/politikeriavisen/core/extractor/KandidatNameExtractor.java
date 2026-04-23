package no.politikeriavisen.core.extractor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import no.politikeriavisen.model.entity.KandidatStortingsvalg;
import no.politikeriavisen.model.repository.KandidatStortingsvalgRepository;

/**
 * Ekstraherer kandidatnavn fra tekst ved å matche mot:
 * 1) kandidater lagret i databasen (KandidatStortingsvalg),
 * 2) sittende regjeringsmedlemmer hentet fra data.stortinget.no,
 * 3) innvalgte stortingsrepresentanter i gjeldende stortingsperiode.
 */
@Component
public class KandidatNameExtractor extends NorwegianNameExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(KandidatNameExtractor.class);

    @Autowired
    private KandidatStortingsvalgRepository kandidatRepository;

    @Autowired
    private StortingApiClient stortingApiClient;

    private Map<String, String> kandidatNamesMap = null;
    private Map<String, String> kjentEtternavnMap = null;

    private static final Pattern ETTERNAVN_PATTERN =
        Pattern.compile("\\b([A-ZÆØÅÁÉÍÓÚÝÞÐ][a-zæøåáéíóúýþðA-ZÆØÅÁÉÍÓÚÝÞÐ]+)\\b");

    /**
     * Standardkonstruktør.
     */
    public KandidatNameExtractor() {
        super();
    }

    /**
     * Laster kandidatnavn fra DB og beriker med navn fra Stortingets API.
     * Kjøres idempotent — mapene bygges kun første gang.
     */
    private void loadKandidatNames() {
        if (kandidatNamesMap != null) {
            return;
        }

        kandidatNamesMap = new HashMap<>();
        kjentEtternavnMap = new HashMap<>();

        // 1) Last kandidater fra databasen — disse har forrang som kanonisk navn
        List<KandidatStortingsvalg> allKandidater = kandidatRepository.findAll();
        for (KandidatStortingsvalg kandidat : allKandidater) {
            if (kandidat.getNavn() != null && !kandidat.getNavn().trim().isEmpty()) {
                String originalName = kandidat.getNavn().trim();
                String lowerCaseName = originalName.toLowerCase();

                kandidatNamesMap.put(lowerCaseName, originalName);

                if (erKjentPolitiker(originalName)) {
                    String etternavn = hentEtternavn(originalName);
                    if (etternavn != null && !etternavn.isEmpty()) {
                        kjentEtternavnMap.put(etternavn.toLowerCase(), originalName);
                    }
                }
            }
        }

        LOGGER.info("Lastet {} kandidater fra databasen", kandidatNamesMap.size());

        // 2) Berik med nåværende regjeringsmedlemmer + stortingsrepresentanter
        beriketMedStortingApi();
    }

    /**
     * Henter sittende regjeringsmedlemmer og stortingsrepresentanter fra
     * data.stortinget.no og legger dem til i kandidat-mappet.
     *
     * Regjeringsmedlemmer får automatisk etternavn-alias (likt som
     * hardkodede kjente politikere), siden de ofte refereres med etternavn
     * alene i artikler (f.eks. "Støre", "Vedum").
     */
    private void beriketMedStortingApi() {
        List<String> regjering = stortingApiClient.hentRegjeringsmedlemmer();
        List<String> storting = stortingApiClient.hentStortingsrepresentanter();

        int foer = kandidatNamesMap.size();

        for (String navn : regjering) {
            leggTilNavnevariant(navn, true);
        }
        for (String navn : storting) {
            leggTilNavnevariant(navn, false);
        }

        LOGGER.info(
            "Beriket kandidatliste fra Stortinget API: {} regjeringsmedlemmer, "
            + "{} representanter. Totalt navn i map: {} (fra {} før).",
            regjering.size(), storting.size(), kandidatNamesMap.size(), foer);
    }

    /**
     * Legger til et navn fra Stortinget-APIet i kandidat-mappet, samt en
     * forkortet variant (første ord + siste ord) for å fange artikler som
     * bruker en kortere form (f.eks. API: "Lubna Boby Jaffery",
     * artikkel: "Lubna Jaffery").
     *
     * Hvis en kort variant allerede finnes i mappet (fra DB), pekes den
     * fulle API-varianten til samme kanoniske navn — DB vinner alltid.
     *
     * @param apiFullName      fullt navn fra Stortinget-API
     * @param erRegjeringsmedlem om personen er regjeringsmedlem (får også
     *                         etternavn-alias)
     */
    private void leggTilNavnevariant(final String apiFullName, final boolean erRegjeringsmedlem) {
        if (apiFullName == null || apiFullName.trim().isEmpty()) {
            return;
        }

        String fullLower = apiFullName.toLowerCase();
        String[] parts = apiFullName.trim().split("\\s+");

        // Finn kanonisk form: hvis DB har kortversjonen "fornavn etternavn",
        // bruk det navnet som canonical; ellers bruk det fulle API-navnet.
        String canonical = apiFullName;
        if (parts.length >= 2) {
            String kortNavn = parts[0] + " " + parts[parts.length - 1];
            String kortLower = kortNavn.toLowerCase();
            String existing = kandidatNamesMap.get(kortLower);
            if (existing != null) {
                canonical = existing;
            } else if (parts.length > 2) {
                // DB hadde ikke kortversjonen — legg den til som alias
                kandidatNamesMap.putIfAbsent(kortLower, canonical);
            }
        }

        // Legg til fulle API-navnet hvis ikke allerede i mappet (DB vinner)
        kandidatNamesMap.putIfAbsent(fullLower, canonical);

        // Regjeringsmedlemmer får etternavn-alias siden avisene ofte
        // skriver f.eks. bare "Støre" eller "Vedum" i løpende tekst.
        if (erRegjeringsmedlem) {
            String etternavn = hentEtternavn(canonical);
            if (etternavn != null && !etternavn.isEmpty()) {
                kjentEtternavnMap.putIfAbsent(etternavn.toLowerCase(), canonical);
            }
        }
    }

    /**
     * Sjekker om en person er en kjent politiker som ofte refereres med bare etternavn.
     *
     * @param fullName Det fullstendige navnet
     * @return true hvis personen er kjent nok til å refereres med bare etternavn
     */
    private boolean erKjentPolitiker(final String fullName) {
        String lowerName = fullName.toLowerCase();

        String[] kjenteNavnFragmenter = {
            "jonas gahr støre", "støre",
            "sylvi listhaug", "listhaug",
            "trygve slagsvold vedum", "vedum",
            "bjørnar moxnes", "moxnes",
            "sandra borch", "borch",
            "peter christian frølich", "frølich"
        };

        for (String fragment : kjenteNavnFragmenter) {
            if (lowerName.contains(fragment)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Henter etternavnet fra et fullstendig navn.
     *
     * @param fullName Det fullstendige navnet
     * @return Etternavnet, eller null hvis ikke funnet
     */
    private String hentEtternavn(final String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return null;
        }

        String[] navneDeler = fullName.trim().split("\\s+");
        if (navneDeler.length > 1) {
            return navneDeler[navneDeler.length - 1];
        }

        return null;
    }

    /**
     * Søker etter kjente etternavn i teksten og returnerer fullstendige navn.
     *
     * @param text Teksten som skal analyseres
     * @return Set med fullstendige navn basert på funnet etternavn
     */
    private Set<String> extractKnownLastNames(final String text) {
        Set<String> foundNames = new HashSet<>();

        if (kjentEtternavnMap == null || kjentEtternavnMap.isEmpty()) {
            return foundNames;
        }

        Matcher matcher = ETTERNAVN_PATTERN.matcher(text);

        while (matcher.find()) {
            String potentialLastName = matcher.group(1);
            String lowerLastName = potentialLastName.toLowerCase();

            if (kjentEtternavnMap.containsKey(lowerLastName)) {
                String fullName = kjentEtternavnMap.get(lowerLastName);
                foundNames.add(fullName);
            }
        }

        return foundNames;
    }

    /**
     * Ekstraherer kandidatnavn fra tekst ved hjelp av regex, database-matching
     * og kjente etternavn.
     *
     * @param text teksten som skal analyseres for kandidatnavn
     * @return sett med ekstraherte kandidatnavn som finnes i databasen
     */
    @Override
    public Set<String> extractNames(final String text) {
        loadKandidatNames();

        Set<String> allFoundNames = new HashSet<>();

        if (kandidatNamesMap != null && !kandidatNamesMap.isEmpty()) {
            List<String> regexNames = super.extractNamesWithRegex(text);

            for (String regexName : regexNames) {
                String normalizedName = regexName.toLowerCase();
                String originalName = kandidatNamesMap.get(normalizedName);

                if (originalName != null) {
                    allFoundNames.add(originalName);
                }
            }
        }

        Set<String> lastNameMatches = extractKnownLastNames(text);
        allFoundNames.addAll(lastNameMatches);

        return allFoundNames;
    }
}
