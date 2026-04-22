package kvasirsbrygg.news_analyzer.analysis.interfaces;

/**
 * Input for analyzers that operate on raw text.
 */
public record TextInput(String text) implements AnalysisInput {

    public TextInput {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text must not be blank");
        }
    }
}
