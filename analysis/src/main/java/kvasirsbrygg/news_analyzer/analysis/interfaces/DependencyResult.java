package kvasirsbrygg.news_analyzer.analysis.interfaces;

import java.time.Instant;
import java.util.List;

/**
 * Result of dependency parsing a sentence.
 *
 * @param tokens list of tokens with dependency information
 * @param timestamp when the analysis was performed
 */
public record DependencyResult(List<DepToken> tokens, Instant timestamp) implements AnalysisResult {

    public DependencyResult(final List<DepToken> tokens) {
        this(tokens, Instant.now());
    }

    /**
     * A token with dependency information.
     *
     * @param id token position (1-indexed)
     * @param form word form
     * @param upos universal POS tag
     * @param head ID of the head token (0 = root)
     * @param deprel dependency relation (nsubj, obj, obl, nmod, etc.)
     */
    public record DepToken(int id, String form, String upos, int head, String deprel) {

        @Override
        public String toString() {
            return String.format("%d\t%s\t%s\t%d\t%s", id, form, upos, head, deprel);
        }
    }
}
