package folkestad.project.analysis;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility klasse for å normalisere partinavnene.
 * Mapper nynorsk versjoner til bokmål-ekvivalenter
 */
public class PartiNameNormalizer {

    private static final Map<String, String> PARTI_MAPPING = new HashMap<>();

    static {
        // Nynorsk til Bokmål mapping (både key og value i title case)
        PARTI_MAPPING.put("Kristeleg Folkeparti", "Kristelig Folkeparti");
        PARTI_MAPPING.put("Framstegspartiet", "Fremskrittspartiet");
        PARTI_MAPPING.put("Arbeidarpartiet", "Arbeiderpartiet");
        PARTI_MAPPING.put("Høgre", "Høyre");
        PARTI_MAPPING.put("Raudt", "Rødt");
        PARTI_MAPPING.put("Miljøpartiet Dei Grøne", "Miljøpartiet De Grønne");

        PARTI_MAPPING.put("Sv - Sosialistisk Venstreparti", "Sosialistisk Venstreparti");

        PARTI_MAPPING.put("ARBEIDERPARTIET", "Arbeiderpartiet");
        PARTI_MAPPING.put("HØYRE", "Høyre");
        PARTI_MAPPING.put("FREMSKRITTSPARTIET", "Fremskrittspartiet");
        PARTI_MAPPING.put("KRISTELIG FOLKEPARTI", "Kristelig Folkeparti");
        PARTI_MAPPING.put("MILJØPARTIET DE GRØNNE", "Miljøpartiet De Grønne");
        PARTI_MAPPING.put("SOSIALISTISK VENSTREPARTI", "Sosialistisk Venstreparti");
        PARTI_MAPPING.put("SENTERPARTIET", "Senterpartiet");
        PARTI_MAPPING.put("VENSTRE", "Venstre");
        PARTI_MAPPING.put("RØDT", "Rødt");

        PARTI_MAPPING.put("KRISTELEG FOLKEPARTI", "Kristelig Folkeparti");
        PARTI_MAPPING.put("FRAMSTEGSPARTIET", "Fremskrittspartiet");
        PARTI_MAPPING.put("ARBEIDARPARTIET", "Arbeiderpartiet");
        PARTI_MAPPING.put("HØGRE", "Høyre");
        PARTI_MAPPING.put("RAUDT", "Rødt");
        PARTI_MAPPING.put("MILJØPARTIET DEI GRØNE", "Miljøpartiet De Grønne");
    }

    /**
     * Normaliserer partinavn til bokmål-versjon med riktig case.
     *
     * @param originalPartiNavn Partinavn som kan være på nynorsk eller bokmål
     * @return Normalisert partinavn på bokmål med stor forbokstav. Returnerer
     *         original hvis input er tom/null.
     */
    public static String normalizePartiName(final String originalPartiNavn) {
        if (originalPartiNavn == null || originalPartiNavn.trim().isEmpty()) {
            return originalPartiNavn;
        }

        String trimmedNavn = originalPartiNavn.trim();

        // Sjekk først om vi har en direct mapping
        if (PARTI_MAPPING.containsKey(trimmedNavn)) {
            return PARTI_MAPPING.get(trimmedNavn);
        }

        // Sjekk uppercase versjon
        String upperCaseNavn = trimmedNavn.toUpperCase();
        if (PARTI_MAPPING.containsKey(upperCaseNavn)) {
            return PARTI_MAPPING.get(upperCaseNavn);
        }

        // Hvis ingen mapping finnes, returner title case
        return toTitleCase(originalPartiNavn);
    }

    /**
     * Konverterer tekst til title case (stor forbokstav, resten små).
     *
     * @param input Tekststreng som skal konverteres
     * @return Tekst i title case. Returnerer original hvis input er tom/null.
     */
    private static String toTitleCase(final String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char ch : input.toCharArray()) {
            if (Character.isWhitespace(ch) || ch == '-') {
                result.append(ch);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(ch));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(ch));
            }
        }

        return result.toString();
    }

    /**
     * Sjekker om et partinavn har en normalisering tilgjengelig.
     *
     * @param partiNavn Partinavn som skal sjekkes
     * @return true hvis partinavn har mapping/normalisering, ellers false
     */
    public static boolean hasMapping(final String partiNavn) {
        if (partiNavn == null || partiNavn.trim().isEmpty()) {
            return false;
        }
        String trimmedNavn = partiNavn.trim();
        return PARTI_MAPPING.containsKey(trimmedNavn)
            || PARTI_MAPPING.containsKey(trimmedNavn.toUpperCase());
    }
}
