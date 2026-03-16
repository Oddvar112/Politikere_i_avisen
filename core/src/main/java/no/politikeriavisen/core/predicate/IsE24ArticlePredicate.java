package no.politikeriavisen.core.predicate;

import java.util.function.Predicate;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Predicate for determining if a given Jsoup Document represents a genuine E24
 * article.
 * <p>
 * All E24 articles have an author byline with specific data-testid attributes.
 * If the document is not an article (e.g., front page), these elements will be
 * missing.
 * </p>
 */
public class IsE24ArticlePredicate implements Predicate<Document> {

    /**
     * Tests whether the provided Jsoup Document is an E24 article.
     *
     * @param doc the Jsoup Document to test
     * @return true if the document is an article, false otherwise
     */
    @Override
    public boolean test(final Document doc) {
        Elements authorNames = doc.select("[data-testid=byline:author-name]");
        if (!authorNames.isEmpty()) {
            String authorText = authorNames.text();
            if (authorText != null && !authorText.trim().isEmpty()) {
                return true;
            }
        }

        return false;
    }
}
