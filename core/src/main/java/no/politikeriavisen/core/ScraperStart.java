package no.politikeriavisen.core;

import java.time.LocalDate;
import java.time.Period;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import no.politikeriavisen.core.extractor.KandidatNameExtractor;
import no.politikeriavisen.core.extractor.StortingApiClient;
import no.politikeriavisen.core.extractor.StortingPerson;
import no.politikeriavisen.core.scraper.DagbladetScraper;
import no.politikeriavisen.core.scraper.E24Scraper;
import no.politikeriavisen.core.scraper.NRKScraper;
import no.politikeriavisen.core.scraper.VGScraper;
import no.politikeriavisen.core.scraper.ScraperFactory;
import no.politikeriavisen.model.entity.KandidatStortingsvalg;
import no.politikeriavisen.model.repository.KandidatStortingsvalgRepository;
import no.politikeriavisen.model.entity.KandidatLink;
import no.politikeriavisen.model.repository.KandidatLinkRepository;
import no.politikeriavisen.model.entity.Nettsted;

/**
 * ScraperStart er ansvarlig for skraping av artikler og utvinning/lagring av personnavn og kandidatnavn.
 * Bruker navnbaserte primærnøkler for kandidater og ScraperFactory for dependency injection.
 */
@Component
public final class ScraperStart {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScraperStart.class);


    @Autowired
    private KandidatNameExtractor kandidatNameExtractor;

    @Autowired
    private KandidatLinkRepository kandidatLinkRepository;

    @Autowired
    private KandidatStortingsvalgRepository kandidatRepository;
    @Autowired
    private ScraperFactory scraperFactory;
    @Autowired
    private StortingApiClient stortingApiClient;

    /**
     * Starter skraping av kandidatnavn fra NRK, VG, E24 og Dagbladet.
     * Bruker navnbaserte primærnøkler for å unngå duplikater.
     */
    public void startScrapingKandidatNames() {
        LOGGER.info("=== Starter scraping av kandidatnavn ===");
        try {
            // Test database tilkobling først
            LOGGER.info("Tester database tilkobling...");
            long kandidatCount = kandidatRepository.count();
            LOGGER.info("Fant {} kandidater i databasen", kandidatCount);

            if (kandidatCount == 0) {
                LOGGER.warn("Ingen kandidater funnet i databasen - kan ikke scrape kandidatnavn");
                return;
            }

            PersonArticleIndex combinedIndex = new PersonArticleIndex();

            // Scrape NRK
            LOGGER.info("Starter NRK scraping...");
            try {
                ArrayList<String> urls = Nettsted.NRK.getAllSourceUrls();
                LOGGER.info("Kobler til NRK: {}", urls);

                NRKScraper nrkScraper = scraperFactory.createNRKScraper(urls);
                LOGGER.info("Bygger NRK indeks...");
                PersonArticleIndex nrkIndex = nrkScraper.buildPersonArticleIndexEfficient(kandidatNameExtractor);

                // Legg til NRK data i kombinert indeks
                LOGGER.info("Fant {} personer i NRK artikler", nrkIndex.getAllPersons().size());
                for (String person : nrkIndex.getAllPersons()) {
                    Set<String> articles = nrkIndex.getArticlesForPerson(person);
                    for (String article : articles) {
                        combinedIndex.addMention(person, article);
                    }
                }

                LOGGER.info("NRK scraping fullført");

            } catch (Exception e) {
                LOGGER.error("Feil under NRK scraping: ", e);
                // Fortsett med VG selv om NRK feiler
            }

            // Scrape VG
            LOGGER.info("Starter VG scraping...");
            try {
                ArrayList<String> vgUrls = Nettsted.VG.getAllSourceUrls();
                LOGGER.info("Kobler til VG: {}", vgUrls);

                VGScraper vgScraper = scraperFactory.createVGScraper(vgUrls);
                LOGGER.info("Bygger VG indeks...");
                PersonArticleIndex vgIndex = vgScraper.buildPersonArticleIndexEfficient(kandidatNameExtractor);

                // Legg til VG data i kombinert indeks
                LOGGER.info("Fant {} personer i VG artikler", vgIndex.getAllPersons().size());
                for (String person : vgIndex.getAllPersons()) {
                    Set<String> articles = vgIndex.getArticlesForPerson(person);
                    for (String article : articles) {
                        combinedIndex.addMention(person, article);
                    }
                }



                LOGGER.info("VG scraping fullført");

            } catch (Exception e) {
                LOGGER.error("Feil under VG scraping: ", e);
            }

            // Scrape E24
            LOGGER.info("Starter E24 scraping...");
            try {
                ArrayList<String> e24Urls = Nettsted.E24.getAllSourceUrls();
                LOGGER.info("Kobler til E24: {}", e24Urls);

                E24Scraper e24Scraper = scraperFactory.createE24Scraper(e24Urls);
                LOGGER.info("Bygger E24 indeks...");
                PersonArticleIndex e24Index = e24Scraper.buildPersonArticleIndexEfficient(kandidatNameExtractor);

                LOGGER.info("Fant {} personer i E24 artikler", e24Index.getAllPersons().size());
                for (String person : e24Index.getAllPersons()) {
                    Set<String> articles = e24Index.getArticlesForPerson(person);
                    for (String article : articles) {
                        combinedIndex.addMention(person, article);
                    }
                }



                LOGGER.info("E24 scraping fullført");

            } catch (Exception e) {
                LOGGER.error("Feil under E24 scraping: ", e);
                // Fortsett med lagring selv om E24 feiler
            }

            // Scrape Dagbladet
            LOGGER.info("Starter Dagbladet scraping...");
            try {
                ArrayList<String> dagbladetUrls = Nettsted.DAGBLADET.getAllSourceUrls();
                LOGGER.info("Kobler til Dagbladet: {}", dagbladetUrls);
                DagbladetScraper dagbladetScraper = scraperFactory.createDagbladetScraper(dagbladetUrls);
                LOGGER.info("Bygger Dagbladet indeks...");
                PersonArticleIndex dagbladetIndex = dagbladetScraper.buildPersonArticleIndexEfficient(kandidatNameExtractor);
                LOGGER.info("Fant {} personer i Dagbladet artikler", dagbladetIndex.getAllPersons().size());
                for (String person : dagbladetIndex.getAllPersons()) {
                    Set<String> articles = dagbladetIndex.getArticlesForPerson(person);
                    for (String article : articles) {
                        combinedIndex.addMention(person, article);
                    }
                }



                LOGGER.info("Dagbladet scraping fullført");
            } catch (Exception e) {
                LOGGER.error("Feil under Dagbladet scraping: ", e);
            }

            LOGGER.info("Totalt fant vi {} unike personer", combinedIndex.getAllPersons().size());
            LOGGER.info("Prosesserer og lagrer kandidater...");
            processAndSaveKandidater(combinedIndex);

            LOGGER.info("=== Scraping av kandidatnavn fullført ===");

        } catch (Exception e) {
            LOGGER.error("KRITISK FEIL under scraping av kandidatnavn: ", e);
            throw e; // Re-throw for bedre debugging
        }
    }

    /**
     * Prosesserer og lagrer kandidater og deres lenker fra PersonArticleIndex.
     *
     * @param personArticleIndex Indeks med kandidater og deres artikler
     */
    private void processAndSaveKandidater(final PersonArticleIndex personArticleIndex) {
        LOGGER.info("=== Prosesserer kandidater ===");

        try {
            Set<String> allKandidatNames = personArticleIndex.getAllPersons();
            LOGGER.info("Behandler {} kandidatnavn", allKandidatNames.size());

            List<KandidatStortingsvalg> existingKandidatList = kandidatRepository.findAllWithLinks();
            LOGGER.info("Hentet {} eksisterende kandidater fra database", existingKandidatList.size());

            Map<String, KandidatStortingsvalg> existingKandidatMap = new java.util.HashMap<>(existingKandidatList
                    .stream()
                    .collect(Collectors.toMap(KandidatStortingsvalg::getNavn, kandidat -> kandidat)));

            // Auto-opprett kandidat-rader for politikere fra Stortinget-APIet
            // som vi har ekstrahert fra tekst men som mangler i databasen.
            // Uten dette ville de blitt droppet i else-grenen under.
            autoOpprettApiKandidater(allKandidatNames, existingKandidatMap);

            List<KandidatLink> kandidatLinksToSave = new ArrayList<>();
            int newLinksCount = 0;

            for (String kandidatName : allKandidatNames) {
                KandidatStortingsvalg kandidat = existingKandidatMap.get(kandidatName);
                if (kandidat != null) {
                    Set<String> articleUrlsForKandidat = personArticleIndex.getArticlesForPerson(kandidatName);

                    Set<String> existingLinks = kandidat.getLinks().stream()
                            .map(link -> link.getLink())
                            .collect(Collectors.toSet());

                    List<String> newLinksForKandidat = new ArrayList<>();
                    for (String articleUrl : articleUrlsForKandidat) {
                        if (!existingLinks.contains(articleUrl)) {
                            KandidatLink kandidatLink = KandidatLink.createWithDetectedNettsted(articleUrl, kandidat);
                            kandidatLinksToSave.add(kandidatLink);
                            newLinksForKandidat.add(articleUrl);
                            newLinksCount++;
                        }
                    }
                    if (!newLinksForKandidat.isEmpty()) {
                        LOGGER.info("Kandidat '{}' får {} nye linker: {}", kandidatName, newLinksForKandidat.size(), newLinksForKandidat);
                    }
                } else {
                    LOGGER.debug("Kandidat '{}' ikke funnet i database", kandidatName);
                }
            }

            LOGGER.info("Fant {} nye kandidatlenker", newLinksCount);

            if (!kandidatLinksToSave.isEmpty()) {
                LOGGER.info("Lagrer {} nye kandidatlenker...", kandidatLinksToSave.size());
                // Anta at du har en kandidatLinkRepository
                kandidatLinkRepository.saveAll(kandidatLinksToSave);
                LOGGER.info("Lagring av kandidatlenker fullført");
            } else {
                LOGGER.info("Ingen nye kandidatlenker å lagre");
            }

        } catch (Exception e) {
            LOGGER.error("Feil under prosessering av kandidater: ", e);
            throw e;
        }
    }

    /**
     * For hvert ekstrahert navn som ikke finnes i databasen, sjekk om
     * Stortinget-APIet har personen (regjeringsmedlem eller representant).
     * Hvis ja — opprett en ny {@link KandidatStortingsvalg}-rad med
     * berikede felter (parti, valgdistrikt, fødselsdato, kjønn, stilling)
     * og legg den til i {@code existingKandidatMap} slik at den videre
     * save-løkka lager {@link KandidatLink} for personen.
     *
     * <p>Dette lukker hullet der politikere som {@code KandidatNameExtractor}
     * finner via API-berikelse ville blitt droppet fordi save-logikken
     * krever en eksisterende rad i {@code kandidat_stortingsvalg}.
     *
     * @param allKandidatNames    alle ekstraherte navn fra artiklene
     * @param existingKandidatMap map som muteres med nye API-genererte rader
     */
    private void autoOpprettApiKandidater(
            final Set<String> allKandidatNames,
            final Map<String, KandidatStortingsvalg> existingKandidatMap) {
        Set<String> manglendeNavn = new HashSet<>();
        for (String navn : allKandidatNames) {
            if (!existingKandidatMap.containsKey(navn)) {
                manglendeNavn.add(navn);
            }
        }
        if (manglendeNavn.isEmpty()) {
            LOGGER.info("Alle {} ekstraherte navn finnes allerede i databasen",
                allKandidatNames.size());
            return;
        }

        LOGGER.info("{} ekstraherte navn mangler i DB — sjekker Stortinget-APIet",
            manglendeNavn.size());

        List<StortingPerson> apiPersoner;
        try {
            apiPersoner = stortingApiClient.hentAllePolitikereDetaljert();
        } catch (Exception e) {
            LOGGER.warn("Kunne ikke hente fra Stortinget-APIet — "
                + "hopper over auto-opprettelse: {}", e.getMessage());
            return;
        }

        Map<String, StortingPerson> apiMap = apiPersoner.stream()
            .collect(Collectors.toMap(
                StortingPerson::fulltNavn,
                p -> p,
                (a, b) -> a));   // regjering vinner over representant ved duplikat

        String periodeLabel = "Stortinget " + stortingApiClient.gjeldendeStortingsperiode();
        List<KandidatStortingsvalg> nyeKandidater = new ArrayList<>();
        for (String navn : manglendeNavn) {
            StortingPerson person = apiMap.get(navn);
            if (person == null) {
                LOGGER.debug("Navn '{}' ikke funnet i API-et — droppes", navn);
                continue;
            }
            KandidatStortingsvalg nyKandidat = byggKandidatFraApi(person, periodeLabel);
            nyeKandidater.add(nyKandidat);
            existingKandidatMap.put(navn, nyKandidat);
            LOGGER.info("Auto-oppretter kandidat fra API: '{}' (parti={}, stilling={})",
                navn, person.partinavn(), person.stilling());
        }

        if (!nyeKandidater.isEmpty()) {
            kandidatRepository.saveAll(nyeKandidater);
            LOGGER.info("Lagret {} nye kandidater fra Stortinget-APIet", nyeKandidater.size());
        }
    }

    /**
     * Bygger en {@link KandidatStortingsvalg} fra en {@link StortingPerson}.
     * Fyller ut alle felter vi har data for fra API-et; udefinerte felter
     * ({@code display_order}, {@code kandidatnr}, {@code bosted}) settes
     * til {@code null}.
     *
     * @param person        politiker fra Stortinget-APIet
     * @param periodeLabel  menneskelesbar periode-etikett, f.eks.
     *                      "Stortinget 2025-2029"
     * @return ny kandidat-entitet klar for persist
     */
    private KandidatStortingsvalg byggKandidatFraApi(
            final StortingPerson person, final String periodeLabel) {
        Integer alder = null;
        if (person.foedselsdato() != null) {
            alder = Period.between(person.foedselsdato(), LocalDate.now()).getYears();
        }
        return KandidatStortingsvalg.builder()
            .navn(person.fulltNavn())
            .valg(periodeLabel)
            .valgdistrikt(person.valgdistrikt())
            .partikode(person.partikode())
            .partinavn(person.partinavn())
            .foedselsdato(person.foedselsdato())
            .alder(alder)
            .kjoenn(person.kjoenn())
            .stilling(person.stilling())
            .build();
    }
}
