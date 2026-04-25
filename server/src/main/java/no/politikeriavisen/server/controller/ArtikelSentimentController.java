package no.politikeriavisen.server.controller;

import java.util.List;

import no.politikeriavisen.dto.PersonSentimentDTO;
import no.politikeriavisen.model.entity.KandidatLink;
import no.politikeriavisen.model.repository.KandidatLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/artikkel")
public class ArtikelSentimentController {

    @Autowired
    private KandidatLinkRepository kandidatLinkRepository;

    /**
     * Henter GIR/FAAR-sentiment for alle politikere nevnt i en gitt artikkel.
     * GET /api/artikkel/sentiment?link=https://vg.no/...
     */
    @GetMapping("/sentiment")
    public ResponseEntity<List<PersonSentimentDTO>> getSentimentForArtikkel(
            @RequestParam("link") final String link) {
        List<KandidatLink> treff = kandidatLinkRepository.findByLink(link);
        if (treff.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<PersonSentimentDTO> result = treff.stream()
                .map(kl -> new PersonSentimentDTO(
                        kl.getKandidat().getNavn(),
                        kl.getGirSentiment(),
                        kl.getGirPositivScore(),
                        kl.getGirNoytralScore(),
                        kl.getGirNegativScore(),
                        kl.getFaarSentiment(),
                        kl.getFaarPositivScore(),
                        kl.getFaarNoytralScore(),
                        kl.getFaarNegativScore()))
                .toList();
        return ResponseEntity.ok(result);
    }
}
