package folkestad.project;

import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import folkestad.project.extractors.KandidatNameExtractor;
// import folkestad.project.extractors.NorwegianNameExtractor; // Removed unused import
import folkestad.project.scrapers.DagbladetScraper;
import folkestad.project.scrapers.E24Scraper;
import folkestad.project.scrapers.NRKScraper;
import folkestad.project.scrapers.VGScraper;
import folkestad.project.scrapers.ScraperFactory;
import folkestad.KandidatStortingsvalg;
import folkestad.KandidatStortingsvalgRepository;
import folkestad.KandidatLink;
import folkestad.KandidatLinkRepository;
import folkestad.Nettsted;

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

            Map<String, KandidatStortingsvalg> existingKandidatMap = existingKandidatList
                    .stream()
                    .collect(Collectors.toMap(KandidatStortingsvalg::getNavn, kandidat -> kandidat));

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
}
