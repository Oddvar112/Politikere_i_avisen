package kvasirsbrygg.news_analyzer.analysis.nlp;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.maltparser.MaltParserService;
import org.springframework.stereotype.Component;

import kvasirsbrygg.news_analyzer.analysis.interfaces.AnalysisException;
import kvasirsbrygg.news_analyzer.analysis.interfaces.Analyzer;
import kvasirsbrygg.news_analyzer.analysis.interfaces.DependencyResult;
import kvasirsbrygg.news_analyzer.analysis.interfaces.DependencyResult.DepToken;
import kvasirsbrygg.news_analyzer.analysis.interfaces.SentenceInput;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

/**
 * Dependency parser: tokenizes, POS-tags, and parses a sentence using OpenNLP + MaltParser.
 * Returns tokens with dependency relations (head, deprel).
 */
@Component
public class DependencyParser implements Analyzer<SentenceInput, DependencyResult> {

    private static final Path MALTPARSER_DIR = Path.of(
            System.getProperty("user.home"), ".cache", "news-analyzer", "maltparser");
    private static final String MALTPARSER_MODEL = "norsk-bokmaal";

    private MaltParserService maltParser;
    private TokenizerME tokenizer;
    private POSTaggerME posTagger;

    @Override
    public DependencyResult analyze(final SentenceInput input) throws AnalysisException {
        try {
            loadModelsIfNeeded();

            String[] tokens = tokenizer.tokenize(input.sentence());
            String[] posTags = posTagger.tag(tokens);

            String[] conlluTokens = new String[tokens.length];
            for (int i = 0; i < tokens.length; i++) {
                conlluTokens[i] = String.format("%d\t%s\t%s\t%s\t%s\t_\t_\t_\t_\t_",
                        i + 1, tokens[i], tokens[i].toLowerCase(), posTags[i], posTags[i]);
            }

            String[] result = maltParser.parseTokens(conlluTokens);

            List<DepToken> depTokens = new ArrayList<>();
            for (String line : result) {
                if (line == null || line.isBlank()) continue;
                String[] fields = line.split("\t");
                if (fields.length >= 10) {
                    int id = parseIntOrZero(fields[0]);
                    int head = parseIntOrZero(fields[fields.length - 2]);
                    String deprel = fields[fields.length - 1];
                    if ("_".equals(deprel)) deprel = "unknown";
                    if (id > 0) {
                        depTokens.add(new DepToken(id, fields[1], fields[3], head, deprel));
                    }
                }
            }
            return new DependencyResult(depTokens);
        } catch (Exception e) {
            throw new AnalysisException("Error during dependency parsing", e);
        }
    }

    private void loadModelsIfNeeded() throws Exception {
        loadTokenizerIfNeeded();
        loadMaltParserIfNeeded();
    }

    private void loadTokenizerIfNeeded() throws Exception {
        if (tokenizer != null) {
            return;
        }

        try (InputStream tokIs = getClass().getResourceAsStream(
                "/opennlp-no-ud-bokmaal-tokens-1.2-2.5.0.bin")) {
            if (tokIs == null) {
                throw new RuntimeException("OpenNLP tokenizer model not found on classpath");
            }
            tokenizer = new TokenizerME(new TokenizerModel(tokIs));
        }

        try (InputStream posIs = getClass().getResourceAsStream(
                "/opennlp-no-ud-bokmaal-pos-1.2-2.5.0.bin")) {
            if (posIs == null) {
                throw new RuntimeException("OpenNLP POS model not found on classpath");
            }
            posTagger = new POSTaggerME(new POSModel(posIs));
        }
    }

    private void loadMaltParserIfNeeded() throws Exception {
        if (maltParser != null) {
            return;
        }

        Path modelFile = MALTPARSER_DIR.resolve(MALTPARSER_MODEL + ".mco");
        if (!Files.exists(modelFile)) {
            throw new RuntimeException(
                    "MaltParser model not found: " + modelFile
                    + ". Run first: cd setup && python setup_maltparser.py");
        }

        maltParser = new MaltParserService();
        maltParser.initializeParserModel(
                "-c " + MALTPARSER_MODEL + " -m parse -w " + MALTPARSER_DIR);
    }

    private static int parseIntOrZero(final String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
