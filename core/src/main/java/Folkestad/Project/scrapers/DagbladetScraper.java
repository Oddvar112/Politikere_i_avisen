package folkestad.project.scrapers;

import java.util.ArrayList;
import java.util.stream.Collectors;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import folkestad.project.PersonArticleIndex;
import folkestad.project.extractors.NorwegianNameExtractor;
import folkestad.project.predicates.IsDagbladetArticlePredicate;

/**
 * DagbladetScraper is a specialized Scraper for extracting articles from
 * Dagbladet RSS feeds.
 * <p>
 * It efficiently processes articles and extracts person names with their
 * associated article links.
 * </p>
 */
public class DagbladetScraper extends Scraper {

    private final IsDagbladetArticlePredicate articlePredicate = new IsDagbladetArticlePredicate();

    /**
     * Constructs a new DagbladetScraper for the given URLs.
     *
     * @param urls the URLs to scrape
     */
    public DagbladetScraper(final ArrayList<String> urls) {
        super(urls);
    }

    /**
     * Henter artikkellenker fra én side (tidligere getLinks-logikk).
     */
    /**
     * Henter artikkellenker fra én side (tidligere getLinks-logikk).
     *
     * @param doc the Jsoup Document to extract links from
     * @return distinct list of article links
     */
    protected ArrayList<String> getlinksFrompage(final Document doc) {
        ArrayList<String> articleLinks = new ArrayList<>();
        Elements articles = doc.select("main article");
        System.out.println("Fant " + articles.size() + " article elementer under main");
        for (Element article : articles) {
            Element link = article.selectFirst("a[href]");
            if (link != null) {
                String url = link.attr("href");
                System.out.println("Fant URL: " + url);
                if (url != null && !url.trim().isEmpty()) {
                    articleLinks.add(url);
                }
            }
        }
        System.out.println("Totalt " + articleLinks.size() + " URL-er lagt til");
        return articleLinks.stream()
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    /**
     *
     * @param doc the article document
     * @return the concatenated text from the article
     */
    public String getAllText(final Document doc) {
        StringBuilder result = new StringBuilder();

        Element titleElement = doc.selectFirst("meta[property=og:title]");
        String title = "";
        if (titleElement != null) {
            title = titleElement.attr("content").trim();
            if (!title.isEmpty()) {
                result.append(title).append(" ");
            }
        }

        // Extract description from og:description meta tag
        Element descriptionElement = doc.selectFirst("meta[property=og:description]");
        String description = "";
        if (descriptionElement != null) {
            description = descriptionElement.attr("content").trim();
            if (!description.isEmpty()) {
                result.append(description).append(" ");
            }
        }

        Element keywordsElement = doc.selectFirst("meta[property=vs:keywords]");
        String keywords = "";
        if (keywordsElement != null) {
            keywords = keywordsElement.attr("content").trim();
        }

        Element articleContent = doc.selectFirst("article");
        if (articleContent != null) {
            articleContent.select(".ad, .advertisement, .promo, nav, header, footer").remove();
            articleContent.select("[class*=ad], [class*=reklame], [class*=annonse]").remove();

            Elements paragraphs = articleContent.select("p");
            for (Element paragraph : paragraphs) {
                String text = paragraph.text().trim();
                if (!text.isEmpty()
                    && !text.toLowerCase().contains("annonse")
                    && !text.toLowerCase().contains("reklame")
                    && text.length() > 20) {
                    result.append(text).append(" ");
                }
            }
        }

        String fullText = result.toString().trim();
        String marker = "Har du tips til oss?";
        int idx = fullText.indexOf(marker);
        if (idx >= 0) {
            fullText = fullText.substring(0, idx).trim();
        }

        if (!keywords.isEmpty()) {
            if (!fullText.isEmpty()) {
                fullText += " " + keywords;
            } else {
                fullText = keywords;
            }
        }

        return fullText;
    }

    /**
     * Effektiv metode som henter artikler og bygger person-artikkel-indeks i én
     * operasjon.
     * Dette unngår å koble seg opp til samme artikkel flere ganger.
     *
     * @param extractor NorwegianNameExtractor-instans
     * @return PersonArticleIndex med alle personer og hvilke artikler de er nevnt i
     */
    public PersonArticleIndex buildPersonArticleIndexEfficient(final NorwegianNameExtractor extractor) {
        return super.buildPersonArticleIndexEfficient(extractor, articlePredicate);
    }

}
