package kvasirsbrygg.news_analyzer.analysis.interfaces;

/**
 * Generic interface for all analysis modules.
 *
 * @param <I> input type
 * @param <O> result type
 */
public interface Analyzer<I extends AnalysisInput, O extends AnalysisResult> {

    /**
     * Perform the analysis.
     */
    O analyze(I input) throws AnalysisException;
}
