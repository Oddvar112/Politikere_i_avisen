package kvasirsbrygg.news_analyzer.analysis.sentiment;

import java.time.Instant;
import java.util.List;

import kvasirsbrygg.news_analyzer.analysis.interfaces.AnalysisResult;

/**
 * Combined sentiment for a person in an article with two dimensions.
 *
 * @param gir sentiment the person expresses (sentences where the person is the grammatical subject)
 * @param faar sentiment the person receives (sentences where the person is mentioned but not subject)
 * @param sentenceCount total number of sentences mentioning the person
 * @param setninger per-setning breakdown (alle setninger som nevner personen,
 *                  inkl. de som SubjectDetector ikke klarte å klassifisere)
 * @param timestamp when the analysis was performed
 */
public record ArticleSentiment(SentimentScore gir, SentimentScore faar,
                               int sentenceCount,
                               List<AnalyzedSentence> setninger,
                               Instant timestamp) implements AnalysisResult {

    public ArticleSentiment(final SentimentScore gir, final SentimentScore faar,
                            final int sentenceCount,
                            final List<AnalyzedSentence> setninger) {
        this(gir, faar, sentenceCount, setninger, Instant.now());
    }

    /**
     * Bakoverkompatibel konstruktør uten setning-liste (gir tom liste).
     * Beholdt for callere som ikke trenger per-setning data.
     */
    public ArticleSentiment(final SentimentScore gir, final SentimentScore faar,
                            final int sentenceCount) {
        this(gir, faar, sentenceCount, List.of(), Instant.now());
    }

    @Override
    public String toString() {
        return String.format("GIR: %s | FAAR: %s (%d sentences)", gir, faar, sentenceCount);
    }
}
