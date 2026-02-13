package folkestad.project.scrapers;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import folkestad.project.PersonArticleIndex;
import folkestad.project.extractors.NorwegianNameExtractor;
import folkestad.project.predicates.IsVgArticlePredicate;

/**
 * VGScraper er en spesialisert skraper for å hente artikler fra VG-forsiden.
 * <p>
 * Den prosesserer artikler og henter personnavn med tilhørende artikkellenker.
 * </p>
 */
public class VGScraper extends Scraper {

    private final IsVgArticlePredicate articlePredicate = new IsVgArticlePredicate();

    /**
     * Oppretter en ny VGScraper for gitte URL-er.
     *
     * @param urls Liste med URL-er som skal skrapes
     */
    public VGScraper(final ArrayList<String> urls) {
        super(urls);
    }

    /**
     * Henter alle artikkellenker fra VG-forsiden ved å skrape artikkelementer under main.
     *
     * @param doc Forside-dokument
     * @return Liste med artikkellenker
     */
    @Override
    protected ArrayList<String> getlinksFrompage(final Document doc) {
        Elements articles = doc.select("main article");
        ArrayList<String> articleLinks = new ArrayList<>();

        for (Element article : articles) {
            Elements links = article.select("a[href]");

            for (Element link : links) {
                String href = link.attr("href");
                String absoluteUrl = link.attr("abs:href");

                if (href != null && !href.trim().isEmpty() && !href.startsWith("#")) {
                    articleLinks.add(absoluteUrl);
                }
            }
        }

        return articleLinks.stream()
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Henter full tekst (overskrift, ingress og brødtekst) fra et VG-artikkeldokument.
     *
     * @param doc Artikkeldokument
     * @return Samlet tekst fra artikkelen
     */
    @Override
    public String getAllText(final Document doc) {
        StringBuilder strongText = new StringBuilder();
        for (Element detailsEl : doc.getElementsByClass("_details_ygndg_24")) {
            for (Element strongEl : detailsEl.select("strong")) {
                String strong = strongEl.text().trim();
                if (!strong.isEmpty()) {
                    strongText.append(strong).append(" ");
                }
            }
        }
        StringBuilder text = new StringBuilder();
        StringBuilder xigzwText = new StringBuilder(); // tags på siden

        Elements mainContent = doc.select("main");

        Elements publishedElements = mainContent.select("*:contains(Publisert)");
        int totalPublishedCount = 0;
        for (Element pub : publishedElements) {
            String pubText = pub.ownText().trim();
            if (pubText.contains("Publisert") || pubText.startsWith("Publisert")) {
                totalPublishedCount++;
            }
        }

        Elements skipContainers = mainContent.select(
                "[class*=reference], "
                + "[class*=related], "
                + "[class*=recommendation], "
                + "[class*=button], "
                + "[class*=controls], "
                + "[class*=player], "
                + "[class*=perspective], "
                + "[class*=astro-island]");

        Elements xigzwElements = mainContent.select("[class*=item][class*=xigzw]");

        Elements headlines = mainContent.select("sectionheader, heading, h1");
        if (!headlines.isEmpty()) {
            text.append(headlines.text()).append(" ");
        }

        Elements allElements = mainContent.select("*");
        int publishedFound = 0;

        for (Element element : allElements) {
            String tagName = element.tagName();
            String ownText = element.ownText().trim();

            if ((ownText.contains("Publisert") || ownText.startsWith("Publisert")) && totalPublishedCount > 0) {
                publishedFound++;
                if (publishedFound == totalPublishedCount) {
                    break;
                }
            }

            boolean isWithinSkipContainer = false;
            for (Element container : skipContainers) {
                if (container.equals(element) || isChildOf(element, container)) {
                    isWithinSkipContainer = true;
                    break;
                }
            }

            if (!isWithinSkipContainer) {
                if (tagName.matches("paragraph|p|sectionheader|time") && !ownText.isEmpty()) {
                        text.append(ownText).append(" ");
                }
            }
        }

        for (Element xigzwEl : xigzwElements) {
            String xigzwElementText = xigzwEl.text().trim();
            if (!xigzwElementText.isEmpty()) {
                xigzwText.append(xigzwElementText).append(" ");
            }
        }

        if (xigzwText.length() > 0) {
            text.append(" ").append(xigzwText.toString());
        }

        if (strongText.length() > 0) {
            String[] names = strongText.toString().trim().split(" ");
            StringBuilder formattedNames = new StringBuilder();
            for (String name : names) {
                if (!name.isEmpty()) {
                    formattedNames.append(Character.toUpperCase(name.charAt(0)));
                    if (name.length() > 1) {
                        formattedNames.append(name.substring(1).toLowerCase());
                    }
                    formattedNames.append(" ");
                }
            }
            text.append(formattedNames.toString().trim()).append(" ");
        }
        return text.toString().trim();
    }


    /**
     * Sjekker om et element er et barn av et gitt container-element.
     *
     * @param element   Elementet som skal sjekkes
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
     * Effektiv metode som henter artikler og bygger person-artikkel-indeks i én operasjon.
     * Dette unngår å koble seg opp til samme artikkel flere ganger.
     *
     * @param extractor NorwegianNameExtractor-instans
     * @return PersonArticleIndex med alle personer og hvilke artikler de er nevnt i
     */
    public PersonArticleIndex buildPersonArticleIndexEfficient(final NorwegianNameExtractor extractor) {
        return super.buildPersonArticleIndexEfficient(extractor, articlePredicate);
    }
}
