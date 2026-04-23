package no.politikeriavisen.core.extractor;

import java.time.LocalDate;

/**
 * DTO som representerer en politiker hentet fra Stortinget-APIet
 * (data.stortinget.no) — enten et regjeringsmedlem eller en
 * stortingsrepresentant.
 *
 * <p>Alle feltene bortsett fra {@code fornavn} og {@code etternavn} er
 * nullbare, siden de to API-endepunktene har litt ulikt feltsett:
 * <ul>
 *   <li>Regjering har {@code stilling} (f.eks. "Finansminister") og
 *       {@code departement}, men ikke {@code valgdistrikt}.</li>
 *   <li>Representanter har {@code valgdistrikt} (fylke), men ikke
 *       {@code stilling}.</li>
 * </ul>
 */
public record StortingPerson(
    String fornavn,
    String etternavn,
    LocalDate foedselsdato,
    String kjoenn,
    String partikode,
    String partinavn,
    String valgdistrikt,
    String stilling,
    boolean erRegjeringsmedlem
) {

    /**
     * Returnerer fullt navn på formen "fornavn etternavn".
     *
     * @return sammensatt navn
     */
    public String fulltNavn() {
        return fornavn + " " + etternavn;
    }
}
