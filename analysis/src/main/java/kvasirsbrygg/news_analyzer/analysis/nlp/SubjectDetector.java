package kvasirsbrygg.news_analyzer.analysis.nlp;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import kvasirsbrygg.news_analyzer.analysis.interfaces.AnalysisException;
import kvasirsbrygg.news_analyzer.analysis.interfaces.Analyzer;
import kvasirsbrygg.news_analyzer.analysis.interfaces.DependencyResult;
import kvasirsbrygg.news_analyzer.analysis.interfaces.DependencyResult.DepToken;
import kvasirsbrygg.news_analyzer.analysis.interfaces.PersonSentenceInput;
import kvasirsbrygg.news_analyzer.analysis.interfaces.SentenceInput;
import kvasirsbrygg.news_analyzer.analysis.interfaces.SubjectResult;

/**
 * Determines if a person expresses opinion (GIR) or receives it (FAAR) in a sentence.
 *
 * Handles: active voice, passive voice, copula, coordination, indirect speech,
 * relative clauses, flat:name/appos, and copula filtering.
 */
@Component
public class SubjectDetector implements Analyzer<PersonSentenceInput, SubjectResult> {

    /**
     * Copula verb forms — nsubj of these → FAAR.
     * Source: Universal Dependencies Norwegian Bokmål treebank defines "være" as
     * the single copula lemma (cop), and "bli" as passive auxiliary (aux:pass).
     * https://universaldependencies.org/treebanks/no_bokmaal/index.html
     */
    private static final Set<String> COPULA_VERBS = Set.of(
            "er", "var", "være", "vært",
            "bli", "ble", "blitt", "blir"
    );

    private final DependencyParser dependencyParser;

    public SubjectDetector(final DependencyParser dependencyParser) {
        this.dependencyParser = dependencyParser;
    }

    @Override
    public SubjectResult analyze(final PersonSentenceInput input) throws AnalysisException {
        DependencyResult parsed = dependencyParser.analyze(new SentenceInput(input.sentence()));
        List<DepToken> tokens = parsed.tokens();
        String[] nameParts = input.personName().toLowerCase().split("\\s+");

        for (DepToken token : tokens) {
            if (!matchesName(token, nameParts)) {
                continue;
            }

            // Resolve the effective deprel and head (follow flat:name, appos, conj chains)
            DepToken resolved = resolveToken(tokens, token);
            if (resolved == null) {
                continue;
            }

            String deprel = resolved.deprel();
            DepToken rootVerb = findRootVerb(tokens, resolved);
            if (rootVerb == null) {
                continue;
            }

            if (deprel.equals("nsubj:pass")) {
                return new SubjectResult(false);
            }
            if ((deprel.equals("obl") || deprel.equals("obl:agent"))
                    && (hasCase(tokens, resolved, "av") || hasCase(tokens, resolved, "ifølge"))) {
                return new SubjectResult(true);
            }
            if (deprel.equals("nsubj")) {
                return new SubjectResult(!isCopula(rootVerb));
            }
            // Fallback: POS tagger may misclassify names (e.g. "Vestre" → ADJ)
            return new SubjectResult(!isCopula(rootVerb));
        }

        return new SubjectResult(false);
    }

    /**
     * Resolves a token through flat:name, appos, and conj chains to find
     * the token that carries the effective dependency relation.
     */
    private DepToken resolveToken(final List<DepToken> tokens, final DepToken token) {
        String deprel = token.deprel();

        // flat:name or appos → follow to head
        if ("flat:name".equals(deprel) || "appos".equals(deprel)) {
            DepToken head = findToken(tokens, token.head());
            if (head == null) {
                return null;
            }
            return resolveToken(tokens, head);
        }

        // conj → follow to head (coordinated element shares role)
        if ("conj".equals(deprel)) {
            DepToken head = findToken(tokens, token.head());
            if (head == null) {
                return null;
            }
            return resolveToken(tokens, head);
        }

        return token;
    }

    private boolean matchesName(final DepToken token, final String[] nameParts) {
        String lowerForm = token.form().toLowerCase();
        for (String part : nameParts) {
            if (lowerForm.equals(part)) {
                return true;
            }
        }
        return false;
    }

    private DepToken findRootVerb(final List<DepToken> tokens, final DepToken resolved) {
        DepToken verb = findToken(tokens, resolved.head());
        return (verb != null && verb.head() == 0) ? verb : null;
    }

    private boolean isCopula(final DepToken verb) {
        return COPULA_VERBS.contains(verb.form().toLowerCase());
    }

    /**
     * Checks if a token has a dependent with deprel="case" and a specific form (e.g. "av").
     */
    private boolean hasCase(final List<DepToken> tokens, final DepToken token, final String caseForm) {
        for (DepToken t : tokens) {
            if (t.head() == token.id()
                    && "case".equals(t.deprel())
                    && caseForm.equalsIgnoreCase(t.form())) {
                return true;
            }
        }
        return false;
    }

    private DepToken findToken(final List<DepToken> tokens, final int id) {
        for (DepToken t : tokens) {
            if (t.id() == id) return t;
        }
        return null;
    }
}
