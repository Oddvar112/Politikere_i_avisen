package no.politikeriavisen.core.scraper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import no.politikeriavisen.core.PersonArticleIndex;
import no.politikeriavisen.core.extractor.NorwegianNameExtractor;
import no.politikeriavisen.core.predicate.IsNrkArticlePredicate;

/**
 * NRKScraper is a specialized Scraper for extracting articles from NRK RSS
 * feeds and article pages.
 * <p>
 * It efficiently processes articles and extracts person names with their
 * associated article links.
 * </p>
 */
public class NRKScraper extends Scraper {

    private final IsNrkArticlePredicate articlePredicate = new IsNrkArticlePredicate();

    /**
     * Oppretter en ny NRKScraper for gitte URLer.
     *
     * @param urls Liste med URLer som skal skrapes
     */
    public NRKScraper(final ArrayList<String> urls) {
        super(urls);
    }

    /**
     * Henter alle artikkellenker fra et RSS-feed-dokument.
     *
     * @param doc RSS-feed-dokument
     * @return Liste med artikkellenker
     */
    @Override
    protected ArrayList<String> getlinksFrompage(final Document doc) {
        Elements links = doc.select("item > link");
        return links.stream().map(link -> link.text()).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Henter full tekst (tittel, brødtekst, bildetekst og forfatter) fra et artikkeldokument.
     *
     * @param doc Artikkeldokument
     * @return Samlet tekst fra artikkelen
     */
    @Override
    public String getAllText(final Document doc) {
        Set<String> paragraphs = new LinkedHashSet<>();

        paragraphs.add(getMetaContent(doc, "og:title"));

        Element article = doc.selectFirst("article");
        if (article != null) {
            extractBodyText(article, paragraphs);
            extractCaptions(article, paragraphs);
        }

        String author = getMetaContent(doc, "author");
        if (!author.isEmpty()) {
            paragraphs.add("Av: " + author);
        }

        paragraphs.remove("");
        return String.join("\n", paragraphs);
    }

    /**
     * Henter alle tekstparagrafer fra artikkelens brødtekst.
     * Bruker fullText (element.text()) slik at lenkede navn fanges opp.
     * Hopper over paragrafer inni aside og nav.
     */
    private void extractBodyText(final Element root, final Set<String> paragraphs) {
        for (Element p : root.select("p")) {
            if (isInsideAny(p, "aside", "nav", "figure")) {
                continue;
            }
            String text = p.text().trim();
            if (!text.isEmpty()) {
                paragraphs.add(text);
            }
        }
    }

    /**
     * Henter bildetekster fra figcaption-elementer.
     * Fanger opp navn som kun er nevnt i bildetekst.
     * LinkedHashSet i kallende metode sørger for at tekst som allerede
     * ble fanget via extractBodyText ikke dupliseres.
     */
    private void extractCaptions(final Element root, final Set<String> paragraphs) {
        for (Element figcaption : root.select("figcaption")) {
            String text = figcaption.text().trim();
            if (!text.isEmpty()) {
                paragraphs.add(text);
            }
        }
    }

    /**
     * Sjekker om et element er barn av et av de oppgitte tag-navnene.
     */
    private boolean isInsideAny(final Element element, final String... tagNames) {
        Element parent = element.parent();
        while (parent != null) {
            for (String tag : tagNames) {
                if (parent.tagName().equals(tag)) {
                    return true;
                }
            }
            parent = parent.parent();
        }
        return false;
    }

    /**
     * Henter innholdet fra en meta-tag i dokumentet.
     *
     * @param doc  Dokumentet
     * @param name Navn på meta-taggen (name eller property)
     * @return Innholdet, eller tom streng hvis ikke funnet
     */
    private String getMetaContent(final Document doc, final String name) {
        String val = doc.select("meta[name=" + name + "]").attr("content");
        if (val.isEmpty()) {
            val = doc.select("meta[property=" + name + "]").attr("content");
        }
        return val.trim();
    }

    /**
     * Henter artikler og bygger person-artikkel-indeks i én operasjon.
     * Unngår å koble seg opp til samme artikkel flere ganger.
     *
     * @param extractor NorwegianNameExtractor-instans
     * @return PersonArticleIndex med alle personer og hvilke artikler de er nevnt i
     */
    public PersonArticleIndex buildPersonArticleIndexEfficient(final NorwegianNameExtractor extractor) {
        return super.buildPersonArticleIndexEfficient(extractor, articlePredicate);
    }
}
