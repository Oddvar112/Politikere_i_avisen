package folkestad.project.TextSummarizer;

/**
 * Holder resultatet av en tekstsammendrag-operasjon.
 * Inneholder selve sammendraget, ordtellinger og kompresjonsrate.
 */
public class SummaryResult {
    /** Sammendragsteksten. */
    private final String summary;
    /** Antall ord i originalteksten. */
    private final int originalWordCount;
    /** Antall ord i sammendraget. */
    private final int summaryWordCount;
    /** Kompresjonsrate (summary/original). */
    private final double compressionRatio;

    /**
     * Oppretter et nytt SummaryResult med alle relevante verdier.
     *
     * @param summary           Sammendragstekst
     * @param originalWordCount Antall ord i originalteksten
     * @param summaryWordCount  Antall ord i sammendraget
     * @param compressionRatio  Kompresjonsrate
     */
    public SummaryResult(final String summary, final int originalWordCount, final int summaryWordCount, final double compressionRatio) {
        this.summary = summary;
        this.originalWordCount = originalWordCount;
        this.summaryWordCount = summaryWordCount;
        this.compressionRatio = compressionRatio;
    }

    /**
     * @return Sammendragsteksten
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @return Antall ord i originalteksten
     */
    public int getOriginalWordCount() {
        return originalWordCount;
    }

    /**
     * @return Antall ord i sammendraget
     */
    public int getSummaryWordCount() {
        return summaryWordCount;
    }

    /**
     * @return Kompresjonsrate (summary/original)
     */
    public double getCompressionRatio() {
        return compressionRatio;
    }
}

