package kvasirsbrygg.news_analyzer.analysis.interfaces;

/**
 * Input for analyzers that operate on a sentence mentioning a specific person.
 */
public record PersonSentenceInput(String sentence, String personName) implements AnalysisInput {

    public PersonSentenceInput {
        if (sentence == null || sentence.isBlank()) {
            throw new IllegalArgumentException("Sentence must not be blank");
        }
        if (personName == null || personName.isBlank()) {
            throw new IllegalArgumentException("Person name must not be blank");
        }
    }
}
