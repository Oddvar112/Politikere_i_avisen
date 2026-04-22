package kvasirsbrygg.news_analyzer.analysis.interfaces;

import java.time.Instant;
import java.util.List;

/**
 * Result containing a list of sentences split from a text.
 */
public record SentencesResult(List<String> sentences, Instant timestamp) implements AnalysisResult {

    public SentencesResult(final List<String> sentences) {
        this(sentences, Instant.now());
    }
}
