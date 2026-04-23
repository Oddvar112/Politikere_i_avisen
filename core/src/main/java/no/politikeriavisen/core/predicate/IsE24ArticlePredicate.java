package no.politikeriavisen.core.predicate;

import java.util.function.Predicate;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Predicate for determining if a given Jsoup Document represents a genuine E24
 * article.
 *
 * <p>Sjekker to mønstre for å identifisere en ekte artikkel:
 * <ol>
 *   <li>Gammel byline via {@code data-testid=byline:author-name} —
 *       beholdt for bakoverkompatibilitet hvis E24 reintroduserer det.</li>
 *   <li>Meta-tag {@code <meta property="article:author">} — alltid
 *       satt på ekte E24-artikler (både Open Graph og faktisk forfatter).
 *       Dette er nåværende E24 DOM-struktur per april 2026.</li>
 * </ol>
 *
 * <p>Forsider og ikke-artikler har ingen av disse.
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

        Elements metaAuthor = doc.select("meta[property='article:author']");
        if (!metaAuthor.isEmpty()) {
            String content = metaAuthor.first().attr("content");
            if (content != null && !content.trim().isEmpty()) {
                return true;
            }
        }

        return false;
    }
}
