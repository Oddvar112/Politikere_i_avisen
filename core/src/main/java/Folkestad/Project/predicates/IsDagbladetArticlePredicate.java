package folkestad.project.predicates;

import java.util.function.Predicate;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Predicate for determining if a given Jsoup Document represents a genuine
 * Dagbladet article.
 * <p>
 * All Dagbladet articles have an article:author meta tag. If the document is
 * not an article,
 * this meta tag will be missing.
 * </p>
 */
public class IsDagbladetArticlePredicate implements Predicate<Document> {

    /**
     * Tests whether the provided Jsoup Document is a Dagbladet article.
     *
     * @param doc the Jsoup Document to test
     * @return true if the document is an article, false otherwise
     */
    @Override
    public boolean test(final Document doc) {
        Elements authorMeta = doc.select("meta[property=article:author]");
        if (!authorMeta.isEmpty()) {
            String authorContent = authorMeta.attr("content");
            return authorContent != null && !authorContent.trim().isEmpty();
        }
        return false;
    }
}
