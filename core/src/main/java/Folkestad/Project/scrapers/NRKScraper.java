package folkestad.project.scrapers;

import java.util.ArrayList;
import java.util.stream.Collectors;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import folkestad.project.PersonArticleIndex;
import folkestad.project.extractors.NorwegianNameExtractor;
import folkestad.project.predicates.IsNrkArticlePredicate;

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
     * Henter full tekst (overskrift, ingress og brødtekst) fra et artikkeldokument.
     *
     * @param doc Artikkeldokument
     * @return Samlet tekst fra artikkelen
     */
    @Override
    public String getAllText(final Document doc) {
        StringBuilder result = new StringBuilder();
        Element articleElement = doc.selectFirst("article");
        if (articleElement == null) {
            return "";
        }
        Elements publishedElements = articleElement.select("*:contains(Publisert)");
        int totalPublishedCount = 0;
        for (Element pub : publishedElements) {
            if (pub.ownText().trim().equals("Publisert")) {
                totalPublishedCount++;
            }
        }
        // Fjernet duplisert og feilaktig deklarasjon av skipContainers
        final Elements skipContainers = articleElement.select(
                "[class*=reference], "
                + "[class*=image], "
                + "[class*=gallery], "
                + "[class*=galleri], "
                + "[class*=article-location], "
                + ".author, "
                + ".authors, "
                + "[class*=article-header-sidebar], "
                + "figure, "
                + "[class*=dh-infosveip], "
                + "[data-name*=dh-infosveip]");
        final Elements allElements = articleElement.select("*");
        int publishedFound = 0;
        for (final Element element : allElements) {
            String tagName = element.tagName();
            String ownText = element.ownText().trim();
            String fullText = element.text().trim();

            if (ownText.equals("Publisert") && totalPublishedCount > 0) {
                publishedFound++;
                if (publishedFound == totalPublishedCount) {
                    break; // Stopp ved siste "Publisert"
                }
            }

            boolean isWithinSkipContainer = false;
            for (final Element container : skipContainers) {
                if (container.equals(element) || isChildOf(element, container)) {
                    isWithinSkipContainer = true;
                    break;
                }
            }

            if (!isWithinSkipContainer) {
                if (tagName.matches("p|div") && !fullText.isEmpty()) {
                    String textToAdd = "";
                    boolean foundSpecialChild = false;

        for (final Element child : element.children()) {
                        if (child.tagName().equals("strong")) {
                            textToAdd = child.text();
                            foundSpecialChild = true;
                            break;
                        }
                    }

                    if (!foundSpecialChild) {
                        for (final Element child : element.children()) {
                            if (child.hasClass("note-container")) {
                                Element noteButton = child.selectFirst(".note-button, button");
                                if (noteButton != null) {
                                    textToAdd = noteButton.text();
                                    foundSpecialChild = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (!foundSpecialChild) {
                        textToAdd = ownText;
                    }

                    if (!textToAdd.isEmpty() && textToAdd.length() > 10) {
                        result.append(textToAdd).append("\n");
                    }
                }
            }
        }

        return result.toString();
    }

    /**
     * Sjekker om et element er et barn av et gitt container-element.
     *
     * @param element Elementet som skal sjekkes
     * @param container Container-elementet
     * @return true hvis element er barn av container, ellers false
     */
    private boolean isChildOf(final Element element, final Element container) {
        Element parent = element.parent();
        while (parent != null) {
            if (parent.equals(container)) {
                return true;
            }
            parent = parent.parent();
        }
        return false;
    }

    /**
     * Henter forfatterinformasjon fra et artikkeldokument.
     *
     * @param doc Artikkeldokument
     * @return Forfatterinfo som streng, eller tom streng hvis ikke funnet
     */
    private String getAuthorInfo(final Document doc) {
        StringBuilder authorInfo = new StringBuilder();
        Element articleElement = doc.selectFirst("article");
        if (articleElement == null) {
            return "";
        }
        Elements authorElements = articleElement.select(
                "[class*=author], [class*=journalist], [class*=byline]");
        for (final Element authorElement : authorElements) {
            String authorText = authorElement.text().trim();
            if (!authorText.isEmpty() && authorText.length() > 3) {
                authorText = authorText.replace("– Journalist", "").replace("- Journalist", "").trim();
                if (!authorText.isEmpty()) {
                    authorInfo.append(authorText).append(", ");
                }
            }
        }
        String result = authorInfo.toString();
        if (result.endsWith(", ")) {
            result = result.substring(0, result.length() - 2);
        }
        if (result.isEmpty()) {
            return "";
        } else {
            return "Skrevet av: " + result;
        }
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
