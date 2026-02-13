package folkestad.project.TextSummarizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * TextSummarizer genererer sammendrag av tekst basert på setnings-score og
 * avsnitt.
 * Fungerer som original SummaryTool, men tar tekst som input.
 */
public class TextSummarizer {

    private ArrayList<Sentence> sentences;
    private ArrayList<Paragraph> paragraphs;
    private ArrayList<Sentence> contentSummary;
    private int noOfSentences;
    private int noOfParagraphs;
    private double[][] intersectionMatrix;
    private LinkedHashMap<Sentence, Double> dictionary;

    /**
     * Oppretter en ny TextSummarizer og initialiserer interne datastrukturer.
     */
    public TextSummarizer() {
        init();
    }

    /**
     * Initialiserer alle interne datastrukturer og teller.
     */
    private void init() {
        sentences = new ArrayList<>();
        paragraphs = new ArrayList<>();
        contentSummary = new ArrayList<>();
        dictionary = new LinkedHashMap<>();
        noOfSentences = 0;
        noOfParagraphs = 0;
    }

    /**
     * Genererer sammendrag fra tekst basert på setnings-score og avsnitt.
     *
     * @param inputText Tekst som skal oppsummeres
     * @return SummaryResult med sammendrag og statistikk
     */
    public SummaryResult summarize(final String inputText) {
        if (inputText == null || inputText.trim().isEmpty()) {
            return new SummaryResult("", 0, 0, 0.0);
        }

        // Reset state
        init();

        try {
            // Følger nøyaktig samme steg som original:
            extractSentenceFromContext(inputText);

            if (sentences.isEmpty()) {
                return new SummaryResult("Ingen setninger funnet i teksten.", 0, 0, 0.0);
            }

            groupSentencesIntoParagraphs();
            createIntersectionMatrix();
            createDictionary();
            createSummary();

            return buildSummaryResult();

        } catch (Exception e) {
            return new SummaryResult("Feil under generering av sammendrag: " + e.getMessage(), 0, 0, 0.0);
        }
    }

    /**
     * Ekstraherer setninger fra tekst og grupperer dem etter avsnitt.
     *
     * @param inputText Tekst som skal deles opp i setninger
     */
    private void extractSentenceFromContext(final String inputText) {
        String[] lines = inputText.split("\n");
        int prevChar = -1;

        StringBuilder currentSentence = new StringBuilder();

        for (String line : lines) {
            for (int i = 0; i < line.length(); i++) {
                char nextChar = line.charAt(i);

                if (nextChar != '.') {
                    currentSentence.append(nextChar);
                } else {
                    // Funnet slutt på setning
                    String sentenceText = currentSentence.toString().trim();
                    if (sentenceText.length() > 0) {
                        sentences.add(new Sentence(noOfSentences, sentenceText, sentenceText.length(), noOfParagraphs));
                        noOfSentences++;
                    }
                    currentSentence.setLength(0);
                }

                // Sjekk for ny paragraf (to linefeed etter hverandre)
                if (nextChar == '\n' && prevChar == '\n') {
                    noOfParagraphs++;
                }

                prevChar = nextChar;
            }

            // Legg til linefeed på slutten av hver linje
            if (currentSentence.length() > 0) {
                currentSentence.append('\n');
            }
            prevChar = '\n';
        }

        // Legg til siste setning hvis den ikke ender med punktum
        String lastSentence = currentSentence.toString().trim();
        if (lastSentence.length() > 0) {
            sentences.add(new Sentence(noOfSentences, lastSentence, lastSentence.length(), noOfParagraphs));
            noOfSentences++;
        }
    }

    /**
     * Grupperer setninger inn i avsnitt basert på avsnittsnummer.
     */
    private void groupSentencesIntoParagraphs() {
        int paraNum = 0;
        Paragraph paragraph = new Paragraph(0);

        for (int i = 0; i < noOfSentences; i++) {
            if (sentences.get(i).getParagraphNumber() != paraNum) {
                paragraphs.add(paragraph);
                paraNum++;
                paragraph = new Paragraph(paraNum);
            }
            // Dummy statement for Checkstyle
            paragraph.getSentences().add(sentences.get(i));
        }

        paragraphs.add(paragraph);
    }

    /**
     * Finner antall felles ord mellom to setninger.
     *
     * @param str1 Første setning
     * @param str2 Andre setning
     * @return Antall felles ord
     */
    private double noOfCommonWords(final Sentence str1, final Sentence str2) {
        double commonCount = 0;

        for (String str1Word : str1.getValue().split("\\s+")) {
            for (String str2Word : str2.getValue().split("\\s+")) {
                if (str1Word.compareToIgnoreCase(str2Word) == 0) {
                    commonCount++;
                }
            }
        }

        return commonCount;
    }

    /**
     * Oppretter matrise med score for felles ord mellom alle setninger.
     */
    private void createIntersectionMatrix() {
        intersectionMatrix = new double[noOfSentences][noOfSentences];
        for (int i = 0; i < noOfSentences; i++) {
            for (int j = 0; j < noOfSentences; j++) {

                if (i <= j) {
                    Sentence str1 = sentences.get(i);
                    Sentence str2 = sentences.get(j);
                    intersectionMatrix[i][j] = noOfCommonWords(str1, str2)
                            / ((double) (str1.getNoOfWords() + str2.getNoOfWords()) / 2);
                } else {
                    intersectionMatrix[i][j] = intersectionMatrix[j][i];
                }
            }
        }
    }

    /**
     * Oppretter dictionary med score for hver setning.
     */
    private void createDictionary() {
        for (int i = 0; i < noOfSentences; i++) {
            double score = 0;
            for (int j = 0; j < noOfSentences; j++) {
                score += intersectionMatrix[i][j];
            }
            dictionary.put(sentences.get(i), score);
            sentences.get(i).setScore(score);
        }
    }

    /**
     * Velger de viktigste setningene fra hvert avsnitt til sammendraget.
     * 1 setning per 5 setninger i hvert avsnitt.
     */
    private void createSummary() {
        for (int j = 0; j <= noOfParagraphs && j < paragraphs.size(); j++) {
            ArrayList<Sentence> paraSentences = paragraphs.get(j).getSentences();
            int primarySet = paraSentences.size() / 5;

            // Sort based on score (importance)
            Collections.sort(paraSentences, new SentenceComparator());

            for (int i = 0; i <= primarySet && i < paraSentences.size(); i++) {
                contentSummary.add(paraSentences.get(i));
            }
        }

        Collections.sort(contentSummary, new SentenceComparatorForSummary());
    }

    /**
     * Bygger resultat-streng og statistikk fra contentSummary.
     *
     * @return SummaryResult med sammendrag og statistikk
     */
    private SummaryResult buildSummaryResult() {
        StringBuilder summary = new StringBuilder();

        for (Sentence sentence : contentSummary) {
            String trimmedValue = sentence.getValue().trim();
            summary.append(trimmedValue);
            if (!trimmedValue.endsWith(".")
                && !trimmedValue.endsWith("!")
                && !trimmedValue.endsWith("?")) {
                summary.append(".");
            }
            summary.append(" ");
        }

        int originalWordCount = getWordCount(sentences);
        int summaryWordCount = getWordCount(contentSummary);
        double compressionRatio;
        if (originalWordCount > 0) {
            compressionRatio = (double) summaryWordCount / originalWordCount;
        } else {
            compressionRatio = 0.0;
        }

        return new SummaryResult(
                summary.toString().trim(),
                originalWordCount,
                summaryWordCount,
                compressionRatio);
    }

    /**
     * Teller antall ord i en liste med setninger.
     *
     * @param sentenceList Liste med setninger
     * @return Totalt antall ord
     */
    private int getWordCount(final ArrayList<Sentence> sentenceList) {
        int wordCount = 0;
        for (Sentence sentence : sentenceList) {
            wordCount += sentence.getNoOfWords();
        }
        return wordCount;
    }

}
