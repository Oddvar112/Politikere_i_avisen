package kvasirsbrygg.news_analyzer.analysis.sentiment;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import kvasirsbrygg.news_analyzer.analysis.nlp.SentenceSplitter;
import kvasirsbrygg.news_analyzer.analysis.nlp.SubjectDetector;
import kvasirsbrygg.news_analyzer.analysis.interfaces.AnalysisException;
import kvasirsbrygg.news_analyzer.analysis.interfaces.Analyzer;
import kvasirsbrygg.news_analyzer.analysis.interfaces.ArticlePersonInput;
import kvasirsbrygg.news_analyzer.analysis.interfaces.PersonSentenceInput;
import kvasirsbrygg.news_analyzer.analysis.interfaces.TextInput;

/**
 * GIR/FAAR analysis: combines dependency parsing (MaltParser) with NorBERT3 sentiment
 * to determine whether a person expresses (GIR) or receives (FAAR) sentiment.
 *
 * <p>For hver setning som nevner personen gjøres NorBERT-inferensen én gang,
 * og resultatet samles i en {@link AnalyzedSentence}-liste slik at persistens-
 * laget kan lagre per-setning data. Setninger der SubjectDetector feiler
 * klassifiseres som {@link AnalyzedSentence.Rolle#UKJENT} og inngår ikke i
 * GIR-/FAAR-aggregatet, men lagres likevel for transparens.
 */
@Component
public class GirFaarAnalyzer implements Analyzer<ArticlePersonInput, ArticleSentiment> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GirFaarAnalyzer.class);

    private static final SentimentScore NEUTRAL = new SentimentScore(0.5, 0.5, 0);

    private final SentenceSplitter sentenceSplitter;
    private final SubjectDetector subjectDetector;
    private final NorBertSentimentAnalyzer sentimentAnalyzer;

    public GirFaarAnalyzer(final SentenceSplitter sentenceSplitter,
                           final SubjectDetector subjectDetector,
                           final NorBertSentimentAnalyzer sentimentAnalyzer) {
        this.sentenceSplitter = sentenceSplitter;
        this.subjectDetector = subjectDetector;
        this.sentimentAnalyzer = sentimentAnalyzer;
    }

    @Override
    public ArticleSentiment analyze(final ArticlePersonInput input) throws AnalysisException {
        List<String> sentences = sentenceSplitter.analyze(new TextInput(input.text())).sentences();

        List<String> nameVariants = buildNameVariants(input);

        List<AnalyzedSentence> analyserteSetninger = new ArrayList<>();
        double girPositivSum = 0;
        double girNegativSum = 0;
        int girCount = 0;
        double faarPositivSum = 0;
        double faarNegativSum = 0;
        int faarCount = 0;

        for (int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i);
            String matchedName = findMatchingName(sentence, nameVariants);
            if (matchedName == null) {
                continue;
            }

            AnalyzedSentence.Rolle rolle = detectRolle(sentence, matchedName);

            SentimentScore score = sentimentAnalyzer.analyze(new TextInput(sentence));

            analyserteSetninger.add(new AnalyzedSentence(
                    sentence,
                    i,
                    matchedName,
                    rolle,
                    score.sentiment(),
                    score.positiveConfidence(),
                    score.negativeConfidence()));

            // Kun setninger vi faktisk klarte å klassifisere bidrar til aggregatet.
            // UKJENT-setninger lagres for transparens, men påvirker ikke GIR/FAAR-snittet.
            if (rolle == AnalyzedSentence.Rolle.GIR) {
                girPositivSum += score.positiveConfidence();
                girNegativSum += score.negativeConfidence();
                girCount++;
            } else if (rolle == AnalyzedSentence.Rolle.FAAR) {
                faarPositivSum += score.positiveConfidence();
                faarNegativSum += score.negativeConfidence();
                faarCount++;
            }
        }

        int klassifisertTotal = girCount + faarCount;
        if (klassifisertTotal == 0) {
            // Ingen setninger klassifisert som GIR/FAAR; returner nøytralt aggregat
            // men bevar eventuelle UKJENT-setninger så de fortsatt kan lagres.
            return new ArticleSentiment(NEUTRAL, NEUTRAL, 0, analyserteSetninger);
        }

        SentimentScore gir = girCount > 0
                ? new SentimentScore(girNegativSum / girCount, girPositivSum / girCount, girCount)
                : NEUTRAL;
        SentimentScore faar = faarCount > 0
                ? new SentimentScore(faarNegativSum / faarCount, faarPositivSum / faarCount, faarCount)
                : NEUTRAL;

        return new ArticleSentiment(gir, faar, klassifisertTotal, analyserteSetninger);
    }

    private AnalyzedSentence.Rolle detectRolle(final String sentence, final String matchedName) {
        try {
            boolean isSubject = subjectDetector.analyze(
                    new PersonSentenceInput(sentence, matchedName)).isSubject();
            return isSubject ? AnalyzedSentence.Rolle.GIR : AnalyzedSentence.Rolle.FAAR;
        } catch (AnalysisException e) {
            LOGGER.debug("SubjectDetector feilet for navn '{}' i setning (markeres UKJENT): {}",
                    matchedName, e.getMessage());
            return AnalyzedSentence.Rolle.UKJENT;
        }
    }

    private List<String> buildNameVariants(final ArticlePersonInput input) {
        List<String> variants = new ArrayList<>();
        variants.add(input.personName());
        String[] nameParts = input.personName().split("\\s+");
        if (nameParts.length > 1) {
            variants.add(nameParts[nameParts.length - 1]);
        }
        variants.addAll(input.aliases());
        return variants;
    }

    private String findMatchingName(final String sentence, final List<String> nameVariants) {
        String lower = sentence.toLowerCase();
        for (String variant : nameVariants) {
            if (lower.contains(variant.toLowerCase())) {
                return variant;
            }
        }
        return null;
    }
}
