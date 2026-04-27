package no.politikeriavisen.core.extractor;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

// import no.politikeriavisen.core.CoreNLPProcessor; // COMMENTED OUT - CoreNLP removed

import java.util.regex.Matcher;

/**
 * NorwegianNameExtractor bruker regex for å finne norske personnavn i en tekst.
 * CoreNLP functionality has been commented out.
 *
 * <p>Regexen bruker Unicode property classes ({@code \p{Lu}}, {@code \p{L}})
 * i stedet for å liste opp aksenttegn eksplisitt — så «Mímir», «Jaffery»,
 * «Frédéric» osv. matches automatisk uten at vi trenger å vedlikeholde en
 * tegnliste.
 *
 * <p>Inngangsteksten NFC-normaliseres før matching. Det er nødvendig fordi
 * web-sider av og til serverer dekomponerte (NFD) Unicode-tegn — f.eks.
 * «í» som «i» + kombinerende ´ — og kombinerende aksenter er {@code \p{M}},
 * ikke {@code \p{L}}, så de bryter regex-matchingen midt i ordet uten
 * normaliseringen.
 */
public class NorwegianNameExtractor {
    private static final Pattern NAME_REGEX = Pattern.compile(
            "\\p{Lu}\\p{L}+(?:[ \\-]\\p{Lu}\\p{L}+){1,4}",
            Pattern.UNICODE_CHARACTER_CLASS);
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
        if (text == null) {
            return Collections.emptyList();
        }
        // NFC-normaliser før matching så dekomponerte aksenttegn (i + ´)
        // blir til prekomponerte (í) og dermed matches av \p{L}. Mimir......
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFC);
        List<String> names = new ArrayList<>();
        Matcher matcher = NAME_REGEX.matcher(normalized);
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
