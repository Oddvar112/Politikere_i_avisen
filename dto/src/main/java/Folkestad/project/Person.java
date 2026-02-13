package folkestad.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Person DTO som representerer en person nevnt i artikler.
 * Inneholder navn, alder, kjønn, parti, valgdistrikt, artikler og antall artikler.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person {

    /** Navn på personen. */
    private String navn;
    /** Alder på personen. */
    private Integer alder;
    /** Kjønn på personen. */
    private String kjoenn;
    /** Parti personen tilhører. */
    private String parti;
    /** Valgdistrikt personen representerer. */
    private String valgdistrikt;
    /** Liste over artikler personen er nevnt i. */
    private List<ArtikelDTO> lenker;
    /** Antall artikler personen er nevnt i. */
    private int antallArtikler;
}

