package kvasirsbrygg.news_analyzer.analysis.interfaces;

import java.time.Instant;

/**
 * Marker interface for all analysis result types.
 */
public interface AnalysisResult {

    /**
     * Timestamp of when the analysis was performed.
     */
    Instant timestamp();
}
