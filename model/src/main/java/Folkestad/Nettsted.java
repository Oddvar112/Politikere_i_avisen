package folkestad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.Getter;

/**
 * Enum som representerer forskjellige nyhetssider med deres URL-mønstre og
 * kildeURLer.
 * Støtter flere URLer per nettsted for kategorier/seksjoner.
 */
@Getter
public enum Nettsted {

    NRK("NRK",
            Arrays.asList(
                    "https://www.nrk.no/toppsaker.rss",
                    "https://www.nrk.no/buskerud/toppsaker.rss",
                    "https://www.nrk.no/buskerud/siste.rss",
                    "https://www.nrk.no/innlandet/toppsaker.rss",
                    "https://www.nrk.no/innlandet/siste.rss",
                    "https://www.nrk.no/mr/toppsaker.rss",
                    "https://www.nrk.no/mr/siste.rss",
                    "https://www.nrk.no/nordland/toppsaker.rss",
                    "https://www.nrk.no/nordland/siste.rss",
                    "https://www.nrk.no/rogaland/toppsaker.rss",
                    "https://www.nrk.no/rogaland/siste.rss",
                    "https://www.nrk.no/stor-oslo/toppsaker.rss",
                    "https://www.nrk.no/stor-oslo/siste.rss",
                    "https://www.nrk.no/sorlandet/toppsaker.rss",
                    "https://www.nrk.no/sorlandet/siste.rss",
                    "https://www.nrk.no/tromsogfinnmark/toppsaker.rss",
                    "https://www.nrk.no/tromsogfinnmark/siste.rss",
                    "https://www.nrk.no/trondelag/toppsaker.rss",
                    "https://www.nrk.no/trondelag/siste.rss",
                    "https://www.nrk.no/vestfoldogtelemark/toppsaker.rss",
                    "https://www.nrk.no/vestfoldogtelemark/siste.rss",
                    "https://www.nrk.no/vestland/toppsaker.rss",
                    "https://www.nrk.no/vestland/siste.rss",
                    "https://www.nrk.no/ostfold/toppsaker.rss",
                    "https://www.nrk.no/ostfold/siste.rss",
                    "https://www.nrk.no/norge/toppsaker.rss",
                    "https://www.nrk.no/urix/toppsaker.rss",
                    "https://www.nrk.no/sapmi/oddasat.rss",
                    "https://www.nrk.no/sport/toppsaker.rss",
                    "https://www.nrk.no/kultur/toppsaker.rss",
                    "https://www.nrk.no/livsstil/toppsaker.rss",
                    "https://www.nrk.no/viten/toppsaker.rss"),
            "nrk.no",
            ScrapingMethod.RSS),

    VG("VG",
            Arrays.asList("https://www.vg.no/"),
            "vg.no",
            ScrapingMethod.FRONTPAGE_DOM),

    E24("E24",
            Arrays.asList(
                    "https://e24.no/",
                    "https://e24.no/privatoekonomi",
                    "https://e24.no/teknologi",
                    "https://e24.no/internasjonal-oekonomi",
                    "https://e24.no/norsk-oekonomi",
                    "https://e24.no/hav-og-sjoemat",
                    "https://e24.no/energi-og-klima",
                    "https://e24.no/naeringsliv",
                    "https://e24.no/boers-og-finans",
                    "https://e24.no/siste"),
            "e24.no",
            ScrapingMethod.FRONTPAGE_DOM),

    AFTENPOSTEN("Aftenposten",
            Arrays.asList("https://www.aftenposten.no/rss"),
            "aftenposten.no",
            ScrapingMethod.RSS),

    DAGBLADET("Dagbladet",
            Arrays.asList("https://www.dagbladet.no/"),
            "dagbladet.no",
            ScrapingMethod.FRONTPAGE_DOM);

    /**
     * Enum for å beskrive hvordan nettsiden scrapers.
     */
    public enum ScrapingMethod {
        RSS, // Scraper via RSS feed
        FRONTPAGE_DOM // Scraper via DOM på frontpage
    }

    private final String displayName;
    private final List<String> sourceUrls; // Liste av URLer (RSS eller frontpage)
    private final String domain;
    private final ScrapingMethod scrapingMethod;

    /**
     * Konstruktør for Nettsted enum.
     *
     * @param displayName Visningsnavn for nettstedet
     * @param sourceUrls Liste med kilde-URLer
     * @param domain Domene for nettstedet
     * @param scrapingMethod Metode for scraping
     */
    Nettsted(final String displayName, final List<String> sourceUrls, final String domain, final ScrapingMethod scrapingMethod) {
        this.displayName = displayName;
        this.sourceUrls = sourceUrls;
        this.domain = domain;
        this.scrapingMethod = scrapingMethod;
    }

    /**
     * Returnerer alle URLer for dette nettstedet.
     */
    /**
     * Returnerer alle kilde-URLer for dette nettstedet.
     * @return Liste med alle kilde-URLer
     */
    public ArrayList<String> getAllSourceUrls() {
        return new ArrayList<>(sourceUrls);
    }

    /**
     * Parser en artikkel-URL og identifiserer hvilken nyhetsside den tilhører.
     */
    /**
     * Parser en artikkel-URL og identifiserer hvilken nyhetsside den tilhører.
     * @param url URL som skal parses
     * @return Optional med identifisert Nettsted
     */
    public static Optional<Nettsted> parseFromUrl(final String url) {
        if (url == null || url.trim().isEmpty()) {
            return Optional.empty();
        }

        String lowerUrl = url.toLowerCase();

        return Arrays.stream(Nettsted.values())
                .filter(nettsted -> lowerUrl.contains(nettsted.domain.toLowerCase()))
                .findFirst();
    }

    /**
     * Returnerer en array med alle RSS-URLer.
     */
    /**
     * Returnerer en array med alle RSS-URLer.
     * @return Array med RSS-URLer
     */
    public static String[] getAllRssUrls() {
        return Arrays.stream(Nettsted.values())
                .filter(nettsted -> nettsted.scrapingMethod == ScrapingMethod.RSS)
                .flatMap(nettsted -> nettsted.sourceUrls.stream())
                .toArray(String[]::new);
    }

    /**
     * Returnerer en array med alle frontpage-URLer.
     */
    /**
     * Returnerer en array med alle frontpage-URLer.
     * @return Array med frontpage-URLer
     */
    public static String[] getAllFrontpageUrls() {
        return Arrays.stream(Nettsted.values())
                .filter(nettsted -> nettsted.scrapingMethod == ScrapingMethod.FRONTPAGE_DOM)
                .flatMap(nettsted -> nettsted.sourceUrls.stream())
                .toArray(String[]::new);
    }

    /**
     * Returnerer alle E24 URLer.
     */
    /**
     * Returnerer alle E24 URLer.
     * @return Array med E24-URLer
     */
    public static String[] getE24Urls() {
        return Arrays.stream(Nettsted.values())
                .filter(nettsted -> nettsted == E24)
                .flatMap(nettsted -> nettsted.sourceUrls.stream())
                .toArray(String[]::new);
    }

    /**
     * Returnerer E24 kategori-URLer (uten hovedsiden).
     */
    /**
     * Returnerer E24 kategori-URLer (uten hovedsiden).
     * @return Array med E24 kategori-URLer
     */
    public static String[] getE24CategoryUrls() {
        return E24.sourceUrls.stream()
                .filter(url -> !url.equals("https://e24.no/"))
                .toArray(String[]::new);
    }

    /**
     * Returnerer alle nettsteder som bruker RSS.
     */
    /**
     * Returnerer alle nettsteder som bruker RSS.
     * @return Array med Nettsted som bruker RSS
     */
    public static Nettsted[] getRssSites() {
        return Arrays.stream(Nettsted.values())
                .filter(nettsted -> nettsted.scrapingMethod == ScrapingMethod.RSS)
                .toArray(Nettsted[]::new);
    }

    /**
     * Returnerer alle nettsteder som bruker frontpage DOM-scraping.
     */
    /**
     * Returnerer alle nettsteder som bruker frontpage DOM-scraping.
     * @return Array med Nettsted som bruker frontpage DOM
     */
    public static Nettsted[] getFrontpageSites() {
        return Arrays.stream(Nettsted.values())
                .filter(nettsted -> nettsted.scrapingMethod == ScrapingMethod.FRONTPAGE_DOM)
                .toArray(Nettsted[]::new);
    }

    /**
     * Returnerer en array med alle tilgjengelige visningsnavn.
     */
    /**
     * Returnerer en array med alle tilgjengelige visningsnavn.
     * @return Array med visningsnavn
     */
    public static String[] getAllDisplayNames() {
        return Arrays.stream(Nettsted.values())
                .map(Nettsted::getDisplayName)
                .toArray(String[]::new);
    }

    /**
     * Finner Nettsted basert på visningsnavn.
     */
    /**
     * Finner Nettsted basert på visningsnavn.
     * @param displayName Visningsnavn
     * @return Optional med identifisert Nettsted
     */
    public static Optional<Nettsted> findByDisplayName(final String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return Optional.empty();
        }

        return Arrays.stream(Nettsted.values())
                .filter(nettsted -> nettsted.displayName.equalsIgnoreCase(displayName.trim()))
                .findFirst();
    }

    /**
     * Kontrollerer om en URL tilhører noen av de kjente nyhetssidene.
     */
    /**
     * Kontrollerer om en URL tilhører noen av de kjente nyhetssidene.
     * @param url URL som skal sjekkes
     * @return true hvis URL tilhører kjent nyhetsside
     */
    public static boolean isKnownNewsSource(final String url) {
        return parseFromUrl(url).isPresent();
    }

    /**
     * Kontrollerer om dette nettstedet bruker RSS.
     */
    /**
     * Kontrollerer om dette nettstedet bruker RSS.
     * @return true hvis RSS brukes
     */
    public boolean usesRss() {
        return this.scrapingMethod == ScrapingMethod.RSS;
    }

    /**
     * Kontrollerer om dette nettstedet bruker frontpage DOM-scraping.
     */
    /**
     * Kontrollerer om dette nettstedet bruker frontpage DOM-scraping.
     * @return true hvis frontpage DOM brukes
     */
    public boolean usesFrontpageDom() {
        return this.scrapingMethod == ScrapingMethod.FRONTPAGE_DOM;
    }

    @Override
    /**
     * Returnerer displayName for dette nettstedet.
     * @return displayName
     */
    public String toString() {
        return displayName + " (" + scrapingMethod + ", " + sourceUrls.size() + " URLs)";
    }
}
