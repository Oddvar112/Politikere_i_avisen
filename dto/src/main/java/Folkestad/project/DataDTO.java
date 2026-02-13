package folkestad.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Map;

/**
 * DTO for å holde analyse-data per kilde (VG, NRK, E24, Dagbladet eller samlet).
 * Inneholder statistikk, personer, kjønn- og partifordeling.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataDTO {
    /** Gjennomsnittlig alder for personer nevnt i artikler. */
    private double gjennomsnittligAlder;
    /** Totalt antall artikler analysert. */
    private int totaltAntallArtikler;
    /** Liste over alle personer nevnt i artikler. */
    private ArrayList<Person> allePersonernevnt;
    /** Kjønnsfordeling (antall per kjønn). */
    private Map<String, Integer> kjoennRatio;
    /** Kjønnsfordeling i prosent. */
    private Map<String, Double> kjoennProsentFordeling;
    /** Partifordeling (antall per parti). */
    private Map<String, Integer> partiMentions;
    /** Partifordeling i prosent. */
    private Map<String, Double> partiProsentFordeling;
    /** Kilde for analysen (VG, NRK, E24, Dagbladet, ALT). */
    private String kilde;
}


