package folkestad.project.TextSummarizer;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

/**
 * Representerer et avsnitt i en tekst, med avsnittsnummer og tilhørende
 * setninger.
 */
@Getter
@Setter
public final class Paragraph {
    /**
     * Avsnittsnummer.
     */
    private int number;
    /**
     * Liste med setninger i avsnittet.
     */
    private ArrayList<Sentence> sentences;

    /**
     * Oppretter et nytt Paragraph-objekt med gitt nummer og tom setningsliste.
     *
     * @param number Avsnittsnummer
     */
    public Paragraph(final int number) {
        this.number = number;
        this.sentences = new ArrayList<>();
    }

    public int getNumber() {
        return number;
    }

    public ArrayList<Sentence> getSentences() {
        return sentences;
    }
}

