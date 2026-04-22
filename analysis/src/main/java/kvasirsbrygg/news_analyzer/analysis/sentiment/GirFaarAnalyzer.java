package kvasirsbrygg.news_analyzer.analysis.sentiment;

import java.util.ArrayList;
import java.util.List;

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
 */
@Component
public class GirFaarAnalyzer implements Analyzer<ArticlePersonInput, ArticleSentiment> {

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

        List<String> girSentences = new ArrayList<>();
        List<String> faarSentences = new ArrayList<>();

        for (String sentence : sentences) {
            String matchedName = findMatchingName(sentence, nameVariants);
            if (matchedName == null) {
                continue;
            }

            boolean isSubject = subjectDetector.analyze(
                    new PersonSentenceInput(sentence, matchedName)).isSubject();

            if (isSubject) {
                girSentences.add(sentence);
            } else {
                faarSentences.add(sentence);
            }
        }

        int total = girSentences.size() + faarSentences.size();
        if (total == 0) {
            SentimentScore neutral = NEUTRAL;
            return new ArticleSentiment(neutral, neutral, 0);
        }

        SentimentScore gir = averageSentiment(girSentences);
        SentimentScore faar = averageSentiment(faarSentences);

        return new ArticleSentiment(gir, faar, total);
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

    private SentimentScore averageSentiment(final List<String> sentences) throws AnalysisException {
        if (sentences.isEmpty()) {
            return NEUTRAL;
        }

        double totalPositive = 0;
        double totalNegative = 0;

        for (String sentence : sentences) {
            SentimentScore score = sentimentAnalyzer.analyze(new TextInput(sentence));
            totalNegative += score.negativeConfidence();
            totalPositive += score.positiveConfidence();
        }

        return new SentimentScore(
                totalNegative / sentences.size(),
                totalPositive / sentences.size(),
                sentences.size());
    }
}
