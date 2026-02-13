package folkestad.project.TextSummarizer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Sentence {
    /**
     * Nummeret til avsnittet denne setningen tilhører.
     */
    private int paragraphNumber;
    /**
     * Setningens nummer i avsnittet.
     */
    private int number;
    /**
     * Lengden på setningen (antall tegn).
     */
    private int stringLength;
    /**
     * Score for setningen (brukes til summering).
     */
    private double score;
    /**
     * Antall ord i setningen.
     */
    private int noOfWords;
    /**
     * Selve setningsteksten.
     */
    private String value;

    /**
     * Oppretter en ny Sentence med gitt nummer, tekst, lengde og avsnittsnummer.
     *
     * @param number          Setningens nummer i avsnittet
     * @param value           Setningstekst
     * @param stringLength    Lengde på setningen (antall tegn)
     * @param paragraphNumber Nummeret til avsnittet
     */
    public Sentence(final int number, final String value, final int stringLength, final int paragraphNumber) {
        this.number = number;
        this.value = value;
        this.stringLength = this.value.length();
        this.noOfWords = this.value.split("\\s+").length;
        this.score = 0.0;
        this.paragraphNumber = paragraphNumber;
    }
}
