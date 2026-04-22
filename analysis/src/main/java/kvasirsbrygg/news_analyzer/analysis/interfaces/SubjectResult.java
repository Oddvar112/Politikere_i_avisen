package kvasirsbrygg.news_analyzer.analysis.interfaces;

import java.time.Instant;

/**
 * Result of checking whether a person is the grammatical subject of the root verb.
 *
 * @param isSubject true if the person is the subject of the root verb
 * @param timestamp when the analysis was performed
 */
public record SubjectResult(boolean isSubject, Instant timestamp) implements AnalysisResult {

    public SubjectResult(final boolean isSubject) {
        this(isSubject, Instant.now());
    }
}
