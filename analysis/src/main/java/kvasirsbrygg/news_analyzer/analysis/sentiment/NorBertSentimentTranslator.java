package kvasirsbrygg.news_analyzer.analysis.sentiment;

import java.nio.file.Path;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

/**
 * DJL Translator for ONNX sentiment models.
 */
class NorBertSentimentTranslator implements Translator<String, SentimentScore> {

    private final Path modelDir;
    private HuggingFaceTokenizer tokenizer;

    NorBertSentimentTranslator(final Path modelDir) {
        this.modelDir = modelDir;
    }

    @Override
    public void prepare(final TranslatorContext ctx) throws Exception {
        tokenizer = HuggingFaceTokenizer.newInstance(modelDir);
    }

    @Override
    public NDList processInput(final TranslatorContext ctx, final String input) {
        ai.djl.huggingface.tokenizers.Encoding encoding = tokenizer.encode(input);
        NDManager manager = ctx.getNDManager();

        long[] inputIds = encoding.getIds();
        long[] attentionMask = encoding.getAttentionMask();

        NDArray inputIdArray = manager.create(inputIds);
        NDArray attentionMaskArray = manager.create(attentionMask);

        return new NDList(inputIdArray, attentionMaskArray);
    }

    @Override
    public SentimentScore processOutput(final TranslatorContext ctx, final NDList list) {
        NDArray logits = list.get(0);

        NDArray exp = logits.exp();
        NDArray probabilities = exp.div(exp.sum());
        float[] probs = probabilities.toFloatArray();

        return new SentimentScore(probs[0], probs[1]);
    }
}
