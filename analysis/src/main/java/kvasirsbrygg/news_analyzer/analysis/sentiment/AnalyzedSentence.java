package kvasirsbrygg.news_analyzer.analysis.sentiment;

import kvasirsbrygg.news_analyzer.domain.Sentiment;

/**
 * Per-setning analyseresultat som leveres fra {@link GirFaarAnalyzer} til
 * persistens-laget. Modellerer én analysert setning med rolle (GIR/FAAR/UKJENT),
 * NorBERT-scorer og den matchende navnvarianten.
 *
 * <p>Holdes i analysis-modulen som en ren DTO slik at model-modulen ikke
 * trenger å avhenge av analysis; service-laget konverterer til
 * {@code AnalysertSetning}-entiteten før persist.
 *
 * @param tekst         setningen slik OpenNLP splittet den
 * @param posisjon      indeks i artikkelen (0-basert) for rekkefølge-bevaring
 * @param matchetNavn   navnvarianten som traff (fullt navn, etternavn, alias)
 * @param rolle         GIR hvis personen er subjekt, FAAR hvis objekt, UKJENT ved parse-feil
 * @param sentiment     klassifisering basert på NorBERT-scorer
 * @param positivScore  NorBERT positiv-konfidensintervall [0.0, 1.0]
 * @param negativScore  NorBERT negativ-konfidensintervall [0.0, 1.0]
 */
public record AnalyzedSentence(
        String tekst,
        int posisjon,
        String matchetNavn,
        Rolle rolle,
        Sentiment sentiment,
        double positivScore,
        double negativScore) {

    public enum Rolle {
        GIR,
        FAAR,
        UKJENT
    }
}
