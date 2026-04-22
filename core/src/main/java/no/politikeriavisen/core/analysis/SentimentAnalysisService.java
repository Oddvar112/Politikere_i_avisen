package no.politikeriavisen.core.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import kvasirsbrygg.news_analyzer.analysis.interfaces.AnalysisException;
import kvasirsbrygg.news_analyzer.analysis.interfaces.ArticlePersonInput;
import kvasirsbrygg.news_analyzer.analysis.sentiment.ArticleSentiment;
import kvasirsbrygg.news_analyzer.analysis.sentiment.GirFaarAnalyzer;
import no.politikeriavisen.model.entity.KandidatLink;
import no.politikeriavisen.model.repository.KandidatLinkRepository;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SentimentAnalysisService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SentimentAnalysisService.class);

    @Autowired(required = false)
    private GirFaarAnalyzer girFaarAnalyzer;

    @Autowired
    private KandidatLinkRepository kandidatLinkRepository;

    @Transactional
    public void analyserUbehandlede() {
        if (girFaarAnalyzer == null) {
            LOGGER.warn("GirFaarAnalyzer ikke tilgjengelig, hopper over sentimentanalyse.");
            return;
        }

        List<KandidatLink> ubehandlede = kandidatLinkRepository.findWithoutSentiment();
        LOGGER.info("Starter sentimentanalyse for {} lenker uten sentiment.", ubehandlede.size());

        Map<String, List<KandidatLink>> gruppertPerUrl = ubehandlede.stream()
                .collect(Collectors.groupingBy(KandidatLink::getLink));

        int lagret = 0;
        for (Map.Entry<String, List<KandidatLink>> entry : gruppertPerUrl.entrySet()) {
            String url = entry.getKey();
            List<KandidatLink> linksForArtikkel = entry.getValue();

            String tekst = hentTekst(url);
            if (tekst == null) {
                continue;
            }

            for (KandidatLink kl : linksForArtikkel) {
                try {
                    String navn = kl.getKandidat().getNavn();
                    ArticleSentiment resultat = girFaarAnalyzer.analyze(
                            new ArticlePersonInput(tekst, navn, byggAliaser(navn)));

                    kl.setGirSentiment(resultat.gir().sentiment().name());
                    kl.setGirPositivScore(resultat.gir().positiveConfidence());
                    kl.setGirNegativScore(resultat.gir().negativeConfidence());

                    kl.setFaarSentiment(resultat.faar().sentiment().name());
                    kl.setFaarPositivScore(resultat.faar().positiveConfidence());
                    kl.setFaarNegativScore(resultat.faar().negativeConfidence());

                    kandidatLinkRepository.save(kl);
                    lagret++;
                } catch (AnalysisException e) {
                    LOGGER.warn("Sentimentanalyse feilet for {} i {}: {}",
                            kl.getKandidat().getNavn(), url, e.getMessage());
                }
            }
        }

        LOGGER.info("Sentimentanalyse fullført: {}/{} lenker analysert.", lagret, ubehandlede.size());
    }

    private String hentTekst(final String link) {
        try {
            String tekst = Jsoup.connect(link)
                    .userAgent("Mozilla/5.0")
                    .timeout(10_000)
                    .get()
                    .text();
            return tekst.isBlank() ? null : tekst;
        } catch (IOException e) {
            LOGGER.warn("Kunne ikke hente artikkel fra {}: {}", link, e.getMessage());
            return null;
        }
    }

    private List<String> byggAliaser(final String fullNavn) {
        String[] deler = fullNavn.split("\\s+");
        List<String> aliaser = new ArrayList<>();
        if (deler.length > 1) {
            aliaser.add(deler[0]);
            aliaser.add(deler[deler.length - 1]);
        }
        return aliaser;
    }
}
