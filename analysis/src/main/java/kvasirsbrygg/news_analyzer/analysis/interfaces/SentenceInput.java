package kvasirsbrygg.news_analyzer.analysis.interfaces;

/**
 * Input for analyzers that operate on a single sentence.
 */
public record SentenceInput(String sentence) implements AnalysisInput {

    public SentenceInput {
        if (sentence == null || sentence.isBlank()) {
            throw new IllegalArgumentException("Sentence must not be blank");
        }
    }
}
