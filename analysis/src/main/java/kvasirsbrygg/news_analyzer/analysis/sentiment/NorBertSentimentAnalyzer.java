package kvasirsbrygg.news_analyzer.analysis.sentiment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import kvasirsbrygg.news_analyzer.analysis.interfaces.AnalysisException;
import kvasirsbrygg.news_analyzer.analysis.interfaces.Analyzer;
import kvasirsbrygg.news_analyzer.analysis.interfaces.TextInput;

/**
 * Sentiment analysis on text via an ONNX model.
 * Model directory must be configured in analysis.properties:
 *   sentiment.model-dir=/path/to/model
 */
@Component
public class NorBertSentimentAnalyzer implements Analyzer<TextInput, SentimentScore> {

    private final Path modelDir;
    private ZooModel<String, SentimentScore> model;

    public NorBertSentimentAnalyzer(
            @Value("${analysis.sentiment.model-dir:#{null}}") final String modelDirConfig) {
        if (modelDirConfig != null && !modelDirConfig.isBlank()) {
            this.modelDir = Path.of(modelDirConfig);
        } else {
            this.modelDir = loadModelDirFromProperties();
        }
    }

    public NorBertSentimentAnalyzer() {
        this.modelDir = loadModelDirFromProperties();
    }

    @Override
    public SentimentScore analyze(final TextInput input) throws AnalysisException {
        try {
            loadModelIfNeeded();
            try (Predictor<String, SentimentScore> predictor = model.newPredictor()) {
                return predictor.predict(input.text());
            }
        } catch (AnalysisException e) {
            throw e;
        } catch (Exception e) {
            throw new AnalysisException("Error during sentiment analysis", e);
        }
    }

    private void loadModelIfNeeded() throws AnalysisException {
        if (model != null) {
            return;
        }

        Path onnxFile = modelDir.resolve("model.onnx");

        if (!Files.exists(onnxFile)) {
            throw new AnalysisException(
                    "ONNX model not found in " + modelDir + ". "
                    + "Run first: cd setup && python export_model.py <model-name>");
        }

        try {
            Criteria<String, SentimentScore> criteria = Criteria.builder()
                    .setTypes(String.class, SentimentScore.class)
                    .optModelPath(modelDir)
                    .optEngine("OnnxRuntime")
                    .optTranslator(new NorBertSentimentTranslator(modelDir))
                    .build();

            model = criteria.loadModel();
        } catch (Exception e) {
            throw new AnalysisException("Failed to load ONNX model from " + modelDir, e);
        }
    }

    private static Path loadModelDirFromProperties() {
        try (InputStream is = NorBertSentimentAnalyzer.class
                .getResourceAsStream("/analysis.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                String dir = props.getProperty("sentiment.model-dir", "").trim();
                if (!dir.isEmpty()) {
                    Path path = Path.of(dir);
                    if (!path.isAbsolute()) {
                        path = Path.of(System.getProperty("user.home")).resolve(path);
                    }
                    return path;
                }
            }
        } catch (IOException ignored) {
        }
        throw new IllegalStateException(
                "No sentiment model configured. Set sentiment.model-dir in analysis.properties");
    }
}
