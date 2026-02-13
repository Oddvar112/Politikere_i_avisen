package folkestad.project;

import java.util.ArrayList;
import java.util.List;
// import java.util.Properties; // Removed unused import
// CoreNLP imports commented out
// import edu.stanford.nlp.ling.CoreAnnotations;
// import edu.stanford.nlp.ling.CoreLabel;
// import edu.stanford.nlp.pipeline.CoreDocument;
// import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * CoreNLPProcessor håndterer all Stanford CoreNLP-funksjonalitet for navnegjenkjenning.
 * Denne klassen er ansvarlig for å konfigurere og kjøre CoreNLP pipeline samt
 * ekstrahering av person-entiteter fra tekst.
 *
 * NOTE: CoreNLP functionality temporarily disabled - methods return empty/dummy values
 */
public class CoreNLPProcessor {
    private static final String PIPELINE_COMPONENTS = "tokenize, ssplit, pos, lemma, ner";
    // private StanfordCoreNLP pipeline;

    public CoreNLPProcessor() {
        // this.pipeline = createPipeline();
        // CoreNLP initialization commented out
    }

    /**
     * Ekstraherer kandidatnavn fra en tekst ved hjelp av CoreNLP.
     * NOTE: Temporarily returns empty list - CoreNLP functionality disabled
     *
     * @param text Teksten som skal analyseres
     * @return Liste med navn funnet av CoreNLP som PERSON-entiteter
     */
    public List<String> extractPersonNames(final String text) {
        /*
        CoreDocument document = new CoreDocument(text);
        pipeline.annotate(document);

        List<String> names = new ArrayList<>();
        StringBuilder currentName = new StringBuilder();

        for (CoreLabel token : document.tokens()) {
            String entity = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
            String word = token.originalText();

            if ("PERSON".equals(entity)) {
                if (currentName.length() > 0) {
                    currentName.append(" ");
                }
                currentName.append(word);
            } else {
                if (currentName.length() > 0) {
                    names.add(currentName.toString());
                    currentName.setLength(0);
                }
            }
        }

        if (currentName.length() > 0) {
            names.add(currentName.toString());
        }

        return names;
        */

        // Return empty list when CoreNLP is disabled
        return new ArrayList<>();
    }

    /**
     * Oppretter og konfigurerer StanfordCoreNLP pipeline.
     * NOTE: Temporarily disabled - returns null
     *
     * @return Konfigurert StanfordCoreNLP pipeline
     */
    private Object createPipeline() {
        /*
        Properties props = new Properties();
        props.setProperty("annotators", PIPELINE_COMPONENTS);
        props.setProperty("coref.algorithm", "neural");
        return new StanfordCoreNLP(props);
        */

        // Return null when CoreNLP is disabled
        return null;
    }
}


