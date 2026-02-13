package folkestad.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import folkestad.project.ScraperStart;
import folkestad.project.analysis.KandidateAnalysis;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@EnableAsync
public class ScheduledScraper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledScraper.class);
    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
    private CompletableFuture<Void> currentScrapingTask = null;

    @Autowired
    private ScraperStart scraperStart;

    @Autowired
    private KandidateAnalysis kandidateAnalysis;

    /**
     * Initialiserer scraping ved applikasjonsstart.
     */
    @PostConstruct
    public void initiateScraping() {
        LOGGER.info("=== Applikasjon startet raskt - scraping vil starte ved første planlagte kjøring ===");
    }


    /**
     * Kjører planlagt scraper med fast intervall.
     */
    @Scheduled(fixedRate = 10800000, initialDelay = 60000) // 3 timer, med 1 min initial delay
    public void runScheduledScraper() {
        if (isShuttingDown.get()) {
            LOGGER.info("Applikasjonen er under avslutning, hopper over planlagt scraping");
            return;
        }

        LOGGER.info("=== Starter planlagt scraper ===");
        runScraperAsync();
    }

    /**
     * Asynkron wrapper for scraping.
     * @return CompletableFuture<Void> som indikerer at scraping er ferdig
     */
    @Async
    public CompletableFuture<Void> runScraperAsync() {
        if (isShuttingDown.get()) {
            LOGGER.info("Applikasjonen er under avslutning, hopper over scraping");
            return CompletableFuture.completedFuture(null);
        }

        currentScrapingTask = CompletableFuture.runAsync(() -> {
            try {
                LOGGER.info("=== Starter asynkron scraping ===");
                long startTime = System.currentTimeMillis();

                // Sjekk shutdown status før hver tung operasjon
                if (!isShuttingDown.get()) {
                    scraperStart.startScrapingKandidatNames();
                }

                if (!isShuttingDown.get()) {
                    LOGGER.info("Starter caching av analyse data...");
                    kandidateAnalysis.analyzeKandidatData();
                    LOGGER.info("Caching av analyse data fullført");
                }

                long endTime = System.currentTimeMillis();
                LOGGER.info("=== Asynkron scraping fullført på {} ms ===", (endTime - startTime));

            } catch (Exception e) {
                LOGGER.error("Feil under asynkron scraping: ", e);
            }
        });

        return currentScrapingTask;
    }

    /**
     * Håndterer graceful shutdown.
     */
    @PreDestroy
    public void onShutdown() {
        LOGGER.info("=== Forbereder avslutning av ScheduledScraper ===");
        isShuttingDown.set(true);

        // Vent på at pågående scraping fullføres (maks 30 sekunder)
        if (currentScrapingTask != null && !currentScrapingTask.isDone()) {
            try {
                LOGGER.info("Venter på at pågående scraping skal fullføres...");
                currentScrapingTask.get(30, java.util.concurrent.TimeUnit.SECONDS);
            } catch (Exception e) {
                LOGGER.warn("Kunne ikke vente på fullføring av scraping: {}", e.getMessage());
                currentScrapingTask.cancel(true);
            }
        }

        LOGGER.info("=== ScheduledScraper avsluttet ===");
    }

    /**
     * Lytter på context closed event for ekstra sikkerhet.
     */
    @EventListener(ContextClosedEvent.class)
    public void onContextClosed() {
        onShutdown();
    }

}
