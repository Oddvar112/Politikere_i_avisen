package kvasirsbrygg.news_analyzer.analysis.interfaces;

/**
 * Checked exception for analysis errors.
 */
public class AnalysisException extends Exception {

    public AnalysisException(final String message) {
        super(message);
    }

    public AnalysisException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
