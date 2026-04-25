package kvasirsbrygg.news_analyzer.analysis.sentiment;

import java.time.Instant;

import kvasirsbrygg.news_analyzer.analysis.interfaces.AnalysisResult;
import kvasirsbrygg.news_analyzer.domain.Sentiment;

/**
 * 3-label sentiment-resultat fra modellen (Cardiff XLM-RoBERTa o.l.):
 * konfidens for negativ, nøytral og positiv. Lagres uavkortet — ingen
 * 3→2 kollaps — slik at databasen og frontenden kan vise samme tre tall
 * som modellen faktisk produserte.
 *
 * <p>For 2-label modeller (kun pos/neg) er det en bakoverkompatibel
 * konstruktør som setter neutral=0.
 */
public record SentimentScore(double negativeConfidence,
                             double neutralConfidence,
                             double positiveConfidence,
                             int mentionCount,
                             Instant timestamp) implements AnalysisResult {

    public SentimentScore(final double negativeConfidence,
                          final double neutralConfidence,
                          final double positiveConfidence) {
        this(negativeConfidence, neutralConfidence, positiveConfidence, 0, Instant.now());
    }

    public SentimentScore(final double negativeConfidence,
                          final double neutralConfidence,
                          final double positiveConfidence,
                          final int mentionCount) {
        this(negativeConfidence, neutralConfidence, positiveConfidence, mentionCount, Instant.now());
    }

    /**
     * Bakoverkompatibel konstruktør for 2-label modeller / eldre kall.
     * Setter neutral = 0.
     */
    public SentimentScore(final double negativeConfidence, final double positiveConfidence) {
        this(negativeConfidence, 0.0, positiveConfidence, 0, Instant.now());
    }

    /**
     * Argmax over de tre konfidensene. Hvis nøytral er størst (eller lik) →
     * NOYTRAL; ellers vinner pos vs neg.
     */
    public Sentiment sentiment() {
        if (neutralConfidence >= negativeConfidence && neutralConfidence >= positiveConfidence) {
            return Sentiment.NOYTRAL;
        }
        return positiveConfidence > negativeConfidence ? Sentiment.POSITIV : Sentiment.NEGATIV;
    }

    /**
     * Beholdt for API-kompatibilitet. Med 3-label data brukes argmax direkte
     * (terskelen ignoreres) — modellen gir oss allerede en eksplisitt
     * nøytral-sannsynlighet.
     */
    public Sentiment sentiment(final double threshold) {
        return sentiment();
    }

    @Override
    public String toString() {
        String base = String.format("negative=%.3f, neutral=%.3f, positive=%.3f -> %s",
                negativeConfidence, neutralConfidence, positiveConfidence, sentiment());
        return mentionCount > 0 ? base + " (" + mentionCount + " sentences)" : base;
    }
}
