package folkestad.project.predicates;

import java.util.function.Predicate;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Predicate for determining if a given Jsoup Document represents a genuine NRK
 * article.
 * <p>
 * All NRK articles have an author. If the document is not an article (e.g.,
 * front page or urix.no),
 * the author will be "NRK" or missing.
 * </p>
 */
public class IsNrkArticlePredicate implements Predicate<Document> {

    /**
     * Tests whether the provided Jsoup Document is an NRK article.
     *
     * @param doc the Jsoup Document to test
     * @return true if the document is an article, false otherwise
     */
    @Override
    public boolean test(final Document doc) {
        boolean isArticle = false;
        Elements metaTags = doc.select("meta[name=author]");
        String authorContent = metaTags.attr("content");
        if (authorContent.isEmpty()) {
            return isArticle;
        }
        if (authorContent.equalsIgnoreCase("nrk") || metaTags.isEmpty()) {
            return isArticle;
        }
        isArticle = true;
        return isArticle;
    }
}
