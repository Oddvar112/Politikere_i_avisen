package kvasirsbrygg.news_analyzer.analysis.sentiment;

import kvasirsbrygg.news_analyzer.domain.Sentiment;

/**
 * Per-setning analyseresultat som leveres fra {@link GirFaarAnalyzer} til
 * persistens-laget. Modellerer én analysert setning med rolle (GIR/FAAR/UKJENT),
 * 3-label sentiment-scorer (neg/nøytral/pos) og den matchende navnvarianten.
 *
 * <p>Holdes i analysis-modulen som en ren DTO slik at model-modulen ikke
 * trenger å avhenge av analysis; service-laget konverterer til
 * {@code AnalysertSetning}-entiteten før persist.
 *
 * @param tekst         setningen slik OpenNLP splittet den
 * @param posisjon      indeks i artikkelen (0-basert) for rekkefølge-bevaring
 * @param matchetNavn   navnvarianten som traff (fullt navn, etternavn, alias)
 * @param rolle         GIR hvis personen er subjekt, FAAR hvis objekt, UKJENT ved parse-feil
 * @param sentiment     argmax-klassifisering basert på de tre konfidensene
 * @param positivScore  modellens positiv-konfidens [0.0, 1.0]
 * @param negativScore  modellens negativ-konfidens [0.0, 1.0]
 * @param noytralScore  modellens nøytral-konfidens [0.0, 1.0] (0 for 2-label modeller)
 */
public record AnalyzedSentence(
        String tekst,
        int posisjon,
        String matchetNavn,
        Rolle rolle,
        Sentiment sentiment,
        double positivScore,
        double negativScore,
        double noytralScore) {

    public enum Rolle {
        GIR,
        FAAR,
        UKJENT
    }
}
