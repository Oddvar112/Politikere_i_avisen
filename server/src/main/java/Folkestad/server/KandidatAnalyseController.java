package folkestad.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import folkestad.project.DataDTO;
import folkestad.project.SammendragDTO;

import java.time.LocalDateTime;

/**
 * REST Controller for kandidat analyse endpoints.
 * Handles only HTTP requests and delegates to service.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/analyse")
public class KandidatAnalyseController {

    @Autowired
    private KandidatAnalyseService kandidatAnalyseService;

    /**
     * Retrieves analysis data for the specified source with optional date filtering.
     * GET /api/analyse/{kilde}?fraDato=2025-01-01T00:00:00&tilDato=2025-01-31T23:59:59
     *
     * @param kilde the source to analyze
     * @param fraDato optional start date for filtering
     * @param tilDato optional end date for filtering
     * @return ResponseEntity containing the analysis data or error status
     */
    @GetMapping("/{kilde}")
    public ResponseEntity<DataDTO> getAnalyseData(
            @PathVariable("kilde") final String kilde,
            @RequestParam(value = "fraDato", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime fraDato,
            @RequestParam(value = "tilDato", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime tilDato) {
        try {
            DataDTO result = kandidatAnalyseService.getAnalyseDataForKilde(kilde, fraDato, tilDato);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
    * Retrieves summary for a given link.
    * GET /api/analyse/sammendrag?link=...
    *

     * @param link the link to summarize
     * @return ResponseEntity containing the summary DTO or NOT_FOUND status
     */
    @GetMapping("/sammendrag")
    public ResponseEntity<SammendragDTO> getSammendragForLink(@RequestParam("link") final String link) {
        SammendragDTO dto = kandidatAnalyseService.getSammendragForLink(link);
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(dto);
    }

}

