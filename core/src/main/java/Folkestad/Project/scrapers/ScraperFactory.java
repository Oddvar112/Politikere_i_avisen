package folkestad.project.scrapers;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import folkestad.InnleggRepository;

@Component
public class ScraperFactory {

    @Autowired
    private InnleggRepository innleggRepository;


    /**
     * Oppretter og returnerer en NRKScraper med repository-injeksjon.
     *
     * @param url Liste med URLer som skal skrapes
     * @return NRKScraper-instans
     */
    public NRKScraper createNRKScraper(final ArrayList<String> url) {
        NRKScraper scraper = new NRKScraper(url);
        scraper.setInnleggRepository(innleggRepository);
        return scraper;
    }


    /**
     * Oppretter og returnerer en VGScraper med repository-injeksjon.
     *
     * @param url Liste med URLer som skal skrapes
     * @return VGScraper-instans
     */
    public VGScraper createVGScraper(final ArrayList<String> url) {
        VGScraper scraper = new VGScraper(url);
        scraper.setInnleggRepository(innleggRepository);
        return scraper;
    }

    /**
     * Oppretter og returnerer en E24Scraper med repository-injeksjon.
     *
     * @param url Liste med URLer som skal skrapes
     * @return E24Scraper-instans
     */
    public E24Scraper createE24Scraper(final ArrayList<String> url) {
        E24Scraper scraper = new E24Scraper(url);
        scraper.setInnleggRepository(innleggRepository);
        return scraper;
    }

    /**
     * Oppretter og returnerer en DagbladetScraper med repository-injeksjon.
     *
     * @param url Liste med URLer som skal skrapes
     * @return DagbladetScraper-instans
     */
    public DagbladetScraper createDagbladetScraper(final ArrayList<String> url) {
        DagbladetScraper scraper = new DagbladetScraper(url);
        scraper.setInnleggRepository(innleggRepository);
        return scraper;
    }
}

// ...existing code...

