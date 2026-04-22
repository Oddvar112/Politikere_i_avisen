package kvasirsbrygg.news_analyzer.analysis.nlp;

import java.io.InputStream;

import org.springframework.stereotype.Component;

import kvasirsbrygg.news_analyzer.analysis.interfaces.AnalysisException;
import kvasirsbrygg.news_analyzer.analysis.interfaces.Analyzer;
import kvasirsbrygg.news_analyzer.analysis.interfaces.SentencesResult;
import kvasirsbrygg.news_analyzer.analysis.interfaces.TextInput;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits text into sentences using OpenNLP Norwegian model.
 */
@Component
public class SentenceSplitter implements Analyzer<TextInput, SentencesResult> {

    private SentenceDetectorME sentenceDetector;

    @Override
    public SentencesResult analyze(final TextInput input) throws AnalysisException {
        try {
            loadModelIfNeeded();
            String[] raw = sentenceDetector.sentDetect(input.text());
            List<String> sentences = new ArrayList<>();
            for (String s : raw) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty() && trimmed.length() > 10) {
                    sentences.add(trimmed);
                }
            }
            return new SentencesResult(sentences);
        } catch (Exception e) {
            throw new AnalysisException("Error during sentence splitting", e);
        }
    }

    private void loadModelIfNeeded() throws Exception {
        if (sentenceDetector != null) {
            return;
        }

        try (InputStream is = getClass().getResourceAsStream(
                "/opennlp-no-ud-bokmaal-sentence-1.2-2.5.0.bin")) {
            if (is == null) {
                throw new RuntimeException("OpenNLP sentence model not found on classpath");
            }
            sentenceDetector = new SentenceDetectorME(new SentenceModel(is));
        }
    }
}
