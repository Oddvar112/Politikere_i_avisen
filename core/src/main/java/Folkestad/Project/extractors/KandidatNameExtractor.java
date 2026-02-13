package folkestad.project.extractors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import folkestad.KandidatStortingsvalg;
import folkestad.KandidatStortingsvalgRepository;

@Component
public class KandidatNameExtractor extends NorwegianNameExtractor {

    @Autowired
    private KandidatStortingsvalgRepository kandidatRepository;

    private Map<String, String> kandidatNamesMap = null;
    private Map<String, String> kjentEtternavnMap = null;
    
    private static final Pattern ETTERNAVN_PATTERN = Pattern.compile("\\b([A-ZÆØÅÁÉÍÓÚÝÞÐ][a-zæøåáéíóúýþðA-ZÆØÅÁÉÍÓÚÝÞÐ]+)\\b");

    public KandidatNameExtractor() {
        super();
    }

    /**
     * Laster kandidatnavn og bygger opp kjente etternavn-map.
     */
    private void loadKandidatNames() {
        if (kandidatNamesMap == null) {
            List<KandidatStortingsvalg> allKandidater = kandidatRepository.findAll();

            kandidatNamesMap = new HashMap<>();
            kjentEtternavnMap = new HashMap<>();

            for (KandidatStortingsvalg kandidat : allKandidater) {
                if (kandidat.getNavn() != null && !kandidat.getNavn().trim().isEmpty()) {
                    String originalName = kandidat.getNavn().trim();
                    String lowerCaseName = originalName.toLowerCase();

                    // Legg til fullstendig navn i normal map
                    kandidatNamesMap.put(lowerCaseName, originalName);

                    // Sjekk om dette er en kjent politiker som ofte refereres med bare etternavn
                    if (erKjentPolitiker(originalName)) {
                        String etternavn = hentEtternavn(originalName);
                        if (etternavn != null && !etternavn.isEmpty()) {
                            kjentEtternavnMap.put(etternavn.toLowerCase(), originalName);
                        }
                    }
                }
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

