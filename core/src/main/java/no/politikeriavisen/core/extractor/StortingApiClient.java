package no.politikeriavisen.core.extractor;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Klient som henter navn og detaljinformasjon om nåværende
 * regjeringsmedlemmer og stortingsrepresentanter fra Stortingets
 * åpne data-API (https://data.stortinget.no).
 *
 * <p>Brukes til å berike kandidat-matchingen slik at vi ikke bare matcher
 * mot kandidater lagret i vår egen database, men også mot sittende
 * rikspolitikere. I tillegg brukes de detaljerte metodene av
 * {@code ScraperStart} til å auto-opprette {@code KandidatStortingsvalg}-
 * rader for politikere som dukker opp i avisen men som ikke finnes i
 * databasen fra før.
 */
@Component
public class StortingApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(StortingApiClient.class);

    private static final String REGJERING_URL =
        "https://data.stortinget.no/eksport/regjering";

    private static final String REPRESENTANTER_BASE_URL =
        "https://data.stortinget.no/eksport/representanter?stortingsperiodeid=";

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);

    /**
     * Stortingsvalg avholdes hvert 4. år på år med (år mod 4 == 1):
     * 2021, 2025, 2029, 2033, ... Det nye Stortinget trer i funksjon 1. oktober.
     */
    private static final int STORTING_PERIODE_LENGDE = 4;
    private static final int VALGAR_MOD = 1;
    private static final Month PERIODE_START_MANED = Month.OCTOBER;
    private static final int PERIODE_START_DAG = 1;

    /**
     * Valgfri overstyring via application.properties:
     *   stortinget.periode=2029-2033
     * Hvis ikke satt, beregnes perioden automatisk fra dagens dato.
     */
    @Value("${stortinget.periode:}")
    private String konfigurertPeriode;

    private final Clock clock;

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    /**
     * Standardkonstruktør — bruker systemklokke for å beregne gjeldende
     * stortingsperiode.
     */
    public StortingApiClient() {
        this(Clock.systemDefaultZone());
    }

    /**
     * Konstruktør som tillater å injisere en egen klokke (brukes i tester
     * for å verifisere periodeberegning).
     *
     * @param injisertKlokke klokke som skal brukes til datosammenligning
     */
    public StortingApiClient(final Clock injisertKlokke) {
        this.clock = injisertKlokke;
    }

    /**
     * Henter fullt navn for alle regjeringsmedlemmer.
     *
     * @return liste med fullt navn (fornavn + etternavn), eller tom liste
     *         hvis APIet ikke er tilgjengelig.
     */
    public List<String> hentRegjeringsmedlemmer() {
        return hentRegjeringsmedlemmerDetaljert().stream()
            .map(StortingPerson::fulltNavn)
            .toList();
    }

    /**
     * Henter fullt navn for alle innvalgte stortingsrepresentanter
     * i gjeldende stortingsperiode.
     *
     * @return liste med fullt navn (fornavn + etternavn), eller tom liste
     *         hvis APIet ikke er tilgjengelig.
     */
    public List<String> hentStortingsrepresentanter() {
        return hentStortingsrepresentanterDetaljert().stream()
            .map(StortingPerson::fulltNavn)
            .toList();
    }

    /**
     * Henter alle regjeringsmedlemmer med fullt feltsett (parti,
     * fødselsdato, kjønn, stilling osv.).
     *
     * @return liste med {@link StortingPerson}, eller tom liste ved feil
     */
    public List<StortingPerson> hentRegjeringsmedlemmerDetaljert() {
        return hentFraApi(REGJERING_URL, "regjeringsmedlem", true);
    }

    /**
     * Henter alle stortingsrepresentanter for gjeldende periode med
     * fullt feltsett (parti, valgdistrikt, fødselsdato, kjønn).
     *
     * @return liste med {@link StortingPerson}, eller tom liste ved feil
     */
    public List<StortingPerson> hentStortingsrepresentanterDetaljert() {
        String periode = gjeldendeStortingsperiode();
        String url = REPRESENTANTER_BASE_URL + periode;
        LOGGER.info("Henter stortingsrepresentanter for periode {}", periode);
        return hentFraApi(url, "representant", false);
    }

    /**
     * Henter alle politikere fra begge API-endepunktene som én samlet
     * liste. Regjeringsmedlemmer kommer først.
     *
     * @return kombinert liste med alle sittende rikspolitikere
     */
    public List<StortingPerson> hentAllePolitikereDetaljert() {
        List<StortingPerson> alle = new ArrayList<>();
        alle.addAll(hentRegjeringsmedlemmerDetaljert());
        alle.addAll(hentStortingsrepresentanterDetaljert());
        return alle;
    }

    /**
     * Returnerer gjeldende stortingsperiode-id. Bruker den konfigurerte
     * verdien fra application.properties hvis satt, ellers beregnes den
     * automatisk ut fra dagens dato.
     *
     * @return stortingsperiode-id på formen "yyyy-yyyy" (f.eks. "2025-2029")
     */
    public String gjeldendeStortingsperiode() {
        if (konfigurertPeriode != null && !konfigurertPeriode.isBlank()) {
            return konfigurertPeriode.trim();
        }
        return beregnStortingsperiode(LocalDate.now(clock));
    }

    /**
     * Beregner stortingsperioden som dekker gitt dato. Siste valgår før
     * (eller lik) datoen finnes ved modulo-regning, og hvis datoen er før
     * 1. oktober i valgåret regnes forrige periode som gjeldende siden
     * det nye Stortinget ikke er trådt i funksjon ennå.
     *
     * @param dato datoen perioden skal beregnes for
     * @return periode-id på formen "yyyy-yyyy"
     */
    static String beregnStortingsperiode(final LocalDate dato) {
        int ar = dato.getYear();
        int sisteValgar = ar - Math.floorMod(ar - VALGAR_MOD, STORTING_PERIODE_LENGDE);
        LocalDate periodeStart = LocalDate.of(
            sisteValgar, PERIODE_START_MANED, PERIODE_START_DAG);
        if (dato.isBefore(periodeStart)) {
            sisteValgar -= STORTING_PERIODE_LENGDE;
        }
        return sisteValgar + "-" + (sisteValgar + STORTING_PERIODE_LENGDE);
    }

    /**
     * Henter og parser XML fra et Stortinget-endepunkt og returnerer
     * {@link StortingPerson}-objekter for hver person.
     *
     * @param url                URL til Stortinget-endepunktet
     * @param elementNavn        XML-elementet som innkapsler hver person
     * @param erRegjeringsmedlem true hvis endepunktet er regjerings-APIet
     * @return liste med personer, eller tom liste ved feil
     */
    private List<StortingPerson> hentFraApi(final String url,
                                            final String elementNavn,
                                            final boolean erRegjeringsmedlem) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/xml")
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                LOGGER.warn("Stortinget API returnerte HTTP {} for {}",
                    response.statusCode(), url);
                return Collections.emptyList();
            }

            return parseXml(response.body(), elementNavn, erRegjeringsmedlem);
        } catch (Exception e) {
            LOGGER.warn("Kunne ikke hente fra {}: {}", url, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Parser XML-respons og bygger {@link StortingPerson} for hvert
     * element.
     *
     * @param xml                XML-innholdet som streng
     * @param elementNavn        tag-navn for personelementene
     * @param erRegjeringsmedlem true hvis endepunktet er regjerings-APIet
     * @return liste med personer
     */
    private List<StortingPerson> parseXml(final String xml,
                                          final String elementNavn,
                                          final boolean erRegjeringsmedlem) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature(
            "http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature(
            "http://xml.org/sax/features/external-parameter-entities", false);
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.parse(new InputSource(new StringReader(xml)));
        NodeList nodes = doc.getElementsByTagName(elementNavn);

        List<StortingPerson> personer = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element element) {
                String fornavn = hentDirekteTekst(element, "fornavn");
                String etternavn = hentDirekteTekst(element, "etternavn");
                if (fornavn.isEmpty() || etternavn.isEmpty()) {
                    continue;
                }

                LocalDate foedselsdato = parseFoedselsdato(
                    hentDirekteTekst(element, "foedselsdato"));
                String kjoenn = tomSomNull(hentDirekteTekst(element, "kjoenn"));

                // parti er et sub-element med id + navn
                Element partiElement = finnDirekteBarnElement(element, "parti");
                String partikode = null;
                String partinavn = null;
                if (partiElement != null) {
                    partikode = tomSomNull(hentDirekteTekst(partiElement, "id"));
                    partinavn = tomSomNull(hentDirekteTekst(partiElement, "navn"));
                }

                String valgdistrikt = null;
                String stilling = null;
                if (erRegjeringsmedlem) {
                    // tittel = f.eks. "Finansminister", "Statsminister"
                    stilling = tomSomNull(hentDirekteTekst(element, "tittel"));
                } else {
                    // representant har fylke som sub-element
                    Element fylkeElement = finnDirekteBarnElement(element, "fylke");
                    if (fylkeElement != null) {
                        valgdistrikt = tomSomNull(
                            hentDirekteTekst(fylkeElement, "navn"));
                    }
                }

                personer.add(new StortingPerson(
                    fornavn,
                    etternavn,
                    foedselsdato,
                    kjoenn,
                    partikode,
                    partinavn,
                    valgdistrikt,
                    stilling,
                    erRegjeringsmedlem
                ));
            }
        }
        return personer;
    }

    /**
     * Parser en ISO-8601-formatert dato-/tid-streng fra API-et
     * (f.eks. "1959-03-16T00:00:00") og returnerer kun dato-delen.
     *
     * @param raw streng fra API-et
     * @return {@link LocalDate}, eller {@code null} hvis parsing feiler
     */
    private LocalDate parseFoedselsdato(final String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            // Plukk datodelen før 'T' — håndterer både "1959-03-16T00:00:00"
            // og "1959-03-16T00:00:00+02:00"
            int tIdx = raw.indexOf('T');
            String datoDel = tIdx > 0 ? raw.substring(0, tIdx) : raw;
            return LocalDate.parse(datoDel);
        } catch (Exception e) {
            LOGGER.debug("Kunne ikke parse fødselsdato '{}': {}", raw, e.getMessage());
            return null;
        }
    }

    private String tomSomNull(final String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    /**
     * Henter tekstinnholdet i et direkte barnelement. Hopper over tagger
     * med samme navn lenger nede i treet (f.eks. 'navn' inne i 'parti').
     *
     * @param parent   foreldre-elementet
     * @param tag      tag-navn å plukke fra
     * @return tekstinnhold eller tom streng
     */
    private String hentDirekteTekst(final Element parent, final String tag) {
        Element child = finnDirekteBarnElement(parent, tag);
        if (child == null) {
            return "";
        }
        String text = child.getTextContent();
        return text != null ? text.trim() : "";
    }

    /**
     * Finner første direkte barnelement med gitt tag-navn.
     *
     * @param parent  foreldre-elementet
     * @param tag     tag-navnet å søke etter
     * @return barnelementet, eller {@code null} hvis ikke funnet
     */
    private Element finnDirekteBarnElement(final Element parent, final String tag) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE
                && tag.equals(child.getNodeName())) {
                return (Element) child;
            }
        }
        return null;
    }
}
