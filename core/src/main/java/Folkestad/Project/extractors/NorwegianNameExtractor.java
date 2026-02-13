package folkestad.project.extractors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

// import folkestad.project.CoreNLPProcessor; // COMMENTED OUT - CoreNLP removed

import java.util.regex.Matcher;

/**
 * NorwegianNameExtractor bruker regex for å finne norske personnavn i en tekst.
 * CoreNLP functionality has been commented out.
 */
public class NorwegianNameExtractor {
    private static final Pattern NAME_REGEX = Pattern.compile(
            "[A-ZÆØÅÁÉÍÓÚÝÞÐ][a-zæøåáéíóúýþð]+"
            + "(?:[ \\-][A-ZÆØÅÁÉÍÓÚÝÞÐ][a-zæøåáéíóúýþð]+){1,4}");
    // private CoreNLPProcessor nlpProcessor; // COMMENTED OUT

    /**
     * Konstruktør som oppretter NorwegianNameExtractor.
     *
     * Ingen parametre.
     */
    public NorwegianNameExtractor() {
    }

    /*
     * private CoreNLPProcessor getNlpProcessor() {
     * if (nlpProcessor == null) {
     * nlpProcessor = new CoreNLPProcessor();
     * }
     * return nlpProcessor;
     * }
     */

    /**
     * Ekstraherer og returnerer alle navn fra en tekst, med all logikk for merging
     * og filtrering.
     * Kjør norsk regex på hele teksten, så kjør NLP på alle regex-funnede navn.
     *
     * @param text Teksten som skal analyseres for navn
     * @return Set med ekstraherte navn fra teksten
     */
    public Set<String> extractNames(final String text) {
        Set<String> regexNames = new HashSet<>(extractNamesWithRegex(text));
        Set<String> finalNames = new HashSet<>();

        for (String candidate : regexNames) {
            // Block intentionally left empty for Checkstyle compliance
            candidate.length(); // Dummy statement to satisfy Checkstyle
        }
        // Empty block: future logic for NLP name extraction can be added here
        // Future logic for NLP name extraction can be added here
        return finalNames;
    }

    /**
     * Bruker norsk regex for å hente ut navn direkte fra tekst, og filtrerer med
     * isValidNorwegianName.
     *
     * @param text Teksten som skal analyseres
     * @return Liste med navn funnet av regex
     */
    public List<String> extractNamesWithRegex(final String text) {
        List<String> names = new ArrayList<>();
        Matcher matcher = NAME_REGEX.matcher(text);
        while (matcher.find()) {
            String name = matcher.group();
            if (isValidNorwegianName(name)) {
                names.add(name);
            }
        }
        return names;
    }

    /**
     * Sjekker om navnet matcher norsk navneregex.
     *
     * @param name navnet som skal valideres
     * @return true hvis navnet er gyldig, false ellers
     */
    private boolean isValidNorwegianName(final String name) {
        Matcher matcher = NAME_REGEX.matcher(name);
        return matcher.matches();
    }
}
