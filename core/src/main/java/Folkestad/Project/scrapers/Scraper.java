package folkestad.project.scrapers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import folkestad.project.PersonArticleIndex;
import folkestad.project.extractors.NorwegianNameExtractor;
import folkestad.project.TextSummarizer.TextSummarizer;
import folkestad.project.TextSummarizer.SummaryResult;
import folkestad.Innlegg;
import folkestad.InnleggRepository;

/**
 * Base class for web scrapers using Jsoup.
 * Nå med integrert text summarization og lagring av sammendrag.
 */
public abstract class Scraper {
    private Document doc;
    private String tekst;
    private ArrayList<String> urls;

    private InnleggRepository innleggRepository;

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Scraper.class);

    private final TextSummarizer textSummarizer = new TextSummarizer();

    /**
     * Constructs a new Scraper for the given URLs.
     *
     * @param urls the URLs to scrape
     */
    public Scraper(final ArrayList<String> urls) {
        this.urls = urls;
    }

    /**
     * Starter skrapingen og henter all tekst fra hoved-URL-en.
     *
     * @param url URL-en som skal skrapes
     */
    public void startScraping(final String url) {
        this.tekst = getAllText(connectToSite(url));
    }

    /**
     * Kobler til gitt URL og returnerer det parse-de Jsoup-dokumentet.
     *
     * @param url URL-en som skal kobles til
     * @return det parse-de dokumentet
     * @throws RuntimeException hvis tilkoblingen feiler
     */
    protected Document connectToSite(final String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            return doc;
        } catch (IOException e) {
            LOGGER.error("Kunne ikke koble til siden: {}", url);
            return null;
        }
    }

    /**
     * Returnerer det sist hentede Jsoup-dokumentet.
     *
     * @return Dokumentet
     */
    public Document getDoc() {
        return this.doc;
    }

    /**
     * Ekstraherer all tekst fra det gitte Jsoup-dokumentet.
     *
     * @param doc Dokumentet det skal hentes tekst fra
     * @return Ekstrahert tekst
     */
    public String getAllText(final Document doc) {
        return doc.text();
    }

    /**
     * Returnerer teksten som er hentet fra skrapingen.
     *
     * @return Hentet tekst
     */
    public String getTekst() {
        return tekst;
    }

    /**
     * Returnerer URL-ene som denne skraperen skal skrape.
     *
     * @return URL-ene
     */
    public ArrayList<String> getUrl() {
        return urls;
    }

    /**
     * Abstrakt metode som subklasser må implementere for å hente lenker fra sin kilde.
     *
     * @param doc Kildedokumentet (RSS-feed, forside, osv.)
     * @return Liste med artikkellenker
     */
    // ...existing code...
    /**
     * Abstract method that subclasses must implement to get links from their source.
     *
     * @param doc the source document (RSS feed, frontpage, etc.)
     * @return list of article links
     */
    protected abstract ArrayList<String> getlinksFrompage(Document doc);
    // ...existing code...

    /**
     * Prosesserer og lagrer sammendraget av en artikkel.
     *
     * @param articleUrl   URL til artikkelen
     * @param originalText Full artikkeltekst
     */
    /**
     * Processes and saves the summary of an article.
     *
     * @param articleUrl   URL of the article
     * @param originalText Full article text
     */
    protected void processAndSaveSummary(final String articleUrl, final String originalText) {
        if (innleggRepository != null && innleggRepository.existsByLink(articleUrl)) {
            return;
        }

        SummaryResult summaryResult = textSummarizer.summarize(originalText);
        Innlegg innlegg = new Innlegg();
        innlegg.setLink(articleUrl);
        innlegg.setSammendragWithStats(summaryResult.getSummary(), originalText);

        if (innleggRepository != null) {
            innleggRepository.save(innlegg);
        }
    }

    /**
     * Effektiv metode som henter artikler og bygger person-artikkel-indeks i én operasjon.
     * Nå med integrert sammendrag-generering og lagring.
     *
     * @param extractor        NorwegianNameExtractor-instans
     * @param articlePredicate Predicate for å filtrere ut kun ekte artikler
     * @return PersonArticleIndex med alle personer og hvilke artikler de er nevnt i
     */
    public PersonArticleIndex buildPersonArticleIndexEfficient(final NorwegianNameExtractor extractor,
            final Predicate<Document> articlePredicate) {
        PersonArticleIndex index = new PersonArticleIndex();

        ArrayList<String> allLinks = getLinks(getUrl());
        ArrayList<String> normalizedLinks = new ArrayList<>();
        for (String link : allLinks) {
            normalizedLinks.add(normalizeUrl(link));
        }

        normalizedLinks.stream()
                .map(this::connectToSite)
                .filter(Objects::nonNull)
                .filter(articlePredicate)
                .forEach(doc -> {
                    String originalUrl = doc.location();
                    String normalizedUrl = normalizeUrl(originalUrl);
                    String text = getAllText(doc);

                    Set<String> names = extractor.extractNames(text);
                    index.addMentions(names, normalizedUrl);
                    if (names != null && !names.isEmpty()) {
                        processAndSaveSummary(normalizedUrl, text);
                    }
                });

        return index;
    }

    /**
     * Sets the InnleggRepository for this scraper.
     * Subclasses may override safely.
     * @param innleggRepository the repository to set
     */
    public void setInnleggRepository(final InnleggRepository innleggRepository) {
        this.innleggRepository = innleggRepository;
    }
    /**
     * Normalizes a URL by removing query parameters. Subclasses may override safely.
     * @param url the URL to normalize
     * @return normalized URL without query parameters
     */
    protected String normalizeUrl(final String url) {
        if (url == null) {
            return null;
        }
        int idxQ = url.indexOf('?');
        String base;
        if (idxQ >= 0) {
            base = url.substring(0, idxQ);
        } else {
            base = url;
        }
        return base;
    }

    /**
     * Gets all article links from the provided URLs. Subclasses may override safely.
     * @param urls list of URLs to scrape
     * @return distinct list of article links
     */
    protected ArrayList<String> getLinks(final ArrayList<String> urls) {
        ArrayList<String> allLinks = new ArrayList<>();
        for (final String url : urls) {
            Document doc = connectToSite(url);
            if (doc != null) {
                allLinks.addAll(getlinksFrompage(doc));
            }
        }
        ArrayList<String> distinctLinks = new ArrayList<>();
        for (String link : allLinks) {
            if (!distinctLinks.contains(link)) {
                distinctLinks.add(link);
            }
        }
        return distinctLinks;
    }
}
