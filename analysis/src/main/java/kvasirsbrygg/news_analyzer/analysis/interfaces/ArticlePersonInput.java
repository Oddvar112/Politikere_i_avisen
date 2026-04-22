package kvasirsbrygg.news_analyzer.analysis.interfaces;

import java.util.List;

/**
 * Input for analyzers that operate on an article mentioning a specific person.
 */
public record ArticlePersonInput(String text, String personName,
                                  List<String> aliases) implements AnalysisInput {

    public ArticlePersonInput {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text must not be blank");
        }
        if (personName == null || personName.isBlank()) {
            throw new IllegalArgumentException("Person name must not be blank");
        }
        if (aliases == null) {
            aliases = List.of();
        }
    }

    public ArticlePersonInput(final String text, final String personName) {
        this(text, personName, List.of());
    }
}
