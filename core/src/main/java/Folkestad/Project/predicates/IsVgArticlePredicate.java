package folkestad.project.predicates;

import java.util.function.Predicate;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Predikat for å avgjøre om et gitt Jsoup Document representerer en ekte VG-artikkel.
 * <p>
 * VG-artikler identifiseres av:
 * 1. HTML-element med data-page-type="Article" attributt (primærsjekk)
 * 2. Reserveløsning: Meta-tagger med forfatterinformasjon (ikke "VG" eller "VG.no")
 * </p>
 */
public class IsVgArticlePredicate implements Predicate<Document> {

    /**
     * Tester om det oppgitte Jsoup Document er en VG-artikkel.
     *
     * @param doc Jsoup Document som skal testes
     * @return true hvis dokumentet er en artikkel, false ellers
     */
    @Override
    public boolean test(final Document doc) {
        Elements htmlElement = doc.select("html[data-page-type=Article]");
        if (!htmlElement.isEmpty()) {
            return true;
        }
        
        Elements authorMeta = doc.select("meta[property=article:author]");
        if (!authorMeta.isEmpty()) {
            String authorContent = authorMeta.attr("content");
            if (authorContent != null
                && !authorContent.trim().isEmpty()
                && !authorContent.equalsIgnoreCase("vg")
                && !authorContent.equalsIgnoreCase("vg.no")) {
                return true;
            }
        }
        Elements authorMetaName = doc.select("meta[name=author]");
        if (!authorMetaName.isEmpty()) {
            String authorContent = authorMetaName.attr("content");
            if (authorContent != null
                && !authorContent.trim().isEmpty()
                && !authorContent.equalsIgnoreCase("vg")
                && !authorContent.equalsIgnoreCase("vg.no")) {
                return true;
            }
        }
        return false;
    }
}
