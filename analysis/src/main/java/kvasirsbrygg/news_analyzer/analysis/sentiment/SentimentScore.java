package kvasirsbrygg.news_analyzer.analysis.sentiment;

import java.time.Instant;

import kvasirsbrygg.news_analyzer.analysis.interfaces.AnalysisResult;
import kvasirsbrygg.news_analyzer.domain.Sentiment;

/**
 * NorBERT3 result: confidence for negative (LABEL_0) and positive (LABEL_1).
 */
public record SentimentScore(double negativeConfidence, double positiveConfidence,
                             int mentionCount, Instant timestamp) implements AnalysisResult {

    private static final double NEUTRAL_THRESHOLD = 0.6;

    public SentimentScore(final double negativeConfidence, final double positiveConfidence) {
        this(negativeConfidence, positiveConfidence, 0, Instant.now());
    }

    public SentimentScore(final double negativeConfidence, final double positiveConfidence,
                          final int mentionCount) {
        this(negativeConfidence, positiveConfidence, mentionCount, Instant.now());
    }

    public Sentiment sentiment(final double threshold) {
        double max = Math.max(negativeConfidence, positiveConfidence);
        if (max < threshold) {
            return Sentiment.NOYTRAL;
        }
        return positiveConfidence > negativeConfidence ? Sentiment.POSITIV : Sentiment.NEGATIV;
    }

    public Sentiment sentiment() {
        return sentiment(NEUTRAL_THRESHOLD);
    }

    @Override
    public String toString() {
        String base = String.format("negative=%.3f, positive=%.3f -> %s",
                negativeConfidence, positiveConfidence, sentiment());
        return mentionCount > 0 ? base + " (" + mentionCount + " sentences)" : base;
    }
}
