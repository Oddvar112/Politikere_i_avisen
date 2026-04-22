package kvasirsbrygg.news_analyzer.analysis;

import java.util.List;

import kvasirsbrygg.news_analyzer.analysis.nlp.DependencyParser;
import kvasirsbrygg.news_analyzer.analysis.nlp.SentenceSplitter;
import kvasirsbrygg.news_analyzer.analysis.nlp.SubjectDetector;
import kvasirsbrygg.news_analyzer.analysis.sentiment.ArticleSentiment;
import kvasirsbrygg.news_analyzer.analysis.sentiment.GirFaarAnalyzer;
import kvasirsbrygg.news_analyzer.analysis.sentiment.NorBertSentimentAnalyzer;
import kvasirsbrygg.news_analyzer.analysis.sentiment.SentimentScore;
import kvasirsbrygg.news_analyzer.analysis.interfaces.ArticlePersonInput;
import kvasirsbrygg.news_analyzer.analysis.interfaces.PersonSentenceInput;
import kvasirsbrygg.news_analyzer.analysis.interfaces.TextInput;
//https://data.stortinget.no/dokumentasjon-og-hjelp/
/**
 * Per-sentence breakdown of GIR/FAAR sentiment analysis.
 *
 * Setup before running:
 *   1. cd setup && python export_model.py
 *   2. cd setup && python setup_maltparser.py
 *   3. ./mvnw exec:java -pl analysis "-Dexec.mainClass=kvasirsbrygg.news_analyzer.analysis.SentimentExample"
 */
public class SentimentExample {

    public static void main(final String[] args) throws Exception {
        SentenceSplitter sentenceSplitter = new SentenceSplitter();
        DependencyParser dependencyParser = new DependencyParser();
        SubjectDetector subjectDetector = new SubjectDetector(dependencyParser);
        NorBertSentimentAnalyzer sentimentAnalyzer = new NorBertSentimentAnalyzer();
        GirFaarAnalyzer girFaarAnalyzer = new GirFaarAnalyzer(
                sentenceSplitter, subjectDetector, sentimentAnalyzer);

        String article = """
                Mímir Kristjánsson hardt ut mot VM-skjenking. \
                Regjeringen vil ha nattåpne ølkraner under fotball-VM. Det er en dårlig idé, \
                mener Rødt-politiker Mímir Kristjánsson, som retter kritikk mot statsministeren. \
                Rødt-politiker Mímir Kristjánsson er kritisk til at det skal kunne serveres øl \
                fram til klokken 06 under fotball-VM. Han mener statsministeren ikke tør å sette ned foten. \
                Mímir Kristjánsson kritiserer forslaget og mener det er et \
                unødvendig inngrep i norsk alkoholpolitikk. \
                Regjeringen foreslår en midlertidig utvidelse av skjenketidene med tre timer under \
                fotball-VM, som spilles i USA, Canada og Mexico. \
                Rødt-politiker Mímir Kristjánsson kritiserer forslaget og mener det er et unødvendig \
                inngrep i norsk alkoholpolitikk. \
                Regjeringen forsvarer forslaget som et unntak for en spesiell begivenhet, og understreker \
                at det er opp til kommunene å avgjøre om de vil ha utvidet skjenketid. \
                Ifølge alkoholloven må utesteder vanligvis stenge skjenkingen kl. 03:00, men ettersom \
                VM-kampene spilles på natten norsk tid, er det foreslått en midlertidig endring. \
                Kristjánsson mener VM brukes som påskudd for å legge om norsk alkoholpolitikk. \
                Regjeringen kjenner de negative konsekvensene av dette. De vet at det skaper masse \
                problemer for utelivet, og ikke minst de som jobber der. \
                I innlegget hevder Rødt-politikeren at både sykefraværet blant menn og vold mot kvinner \
                øker under store internasjonale mesterskap. \
                Men alt dette skal vi altså drite i, fordi Jonas Gahr Støre ikke er voksen nok \
                til å være festbrems. \
                Norge har og skal fortsette å ha en restriktiv alkoholpolitikk, sier statssekretær \
                Usman Mushtaq i en e-post til NRK. \
                Regjeringen har foreslått en midlertidig endring av alkoholloven under sommerens \
                fotball-VM som gir kommuner anledning til å tillate skjenking av øl og vin i tidsrommet \
                mellom klokken tre og seks om natten, skriver han. \
                Vi trenger ikke staten som partyfikser, men for å sette noen grenser for det som helt \
                sikkert uansett kommer til å være tidenes folkefest i Norge. \
                Kristjansson vil ikke uttale seg til NRK utover det som står i Facebook-innlegget. \
                Det var uken før jul at statsminister Jonas Gahr Støre lovet at alle nordmenn \
                skulle få se ferdig VM-kampene og drikke øl til fløyta blir blåst. \
                Sitter du på en pub eller ser kampen med venner, skal du få se kampen ferdig, \
                drikke opp ølen og gjerne kjøpe en til, sa Støre i NRKs Politisk Kvarter. \
                Selv om forslaget fra regjeringen går gjennom, er det opp til hver enkelt kommune å \
                ta stilling til om de vil tillate en utvidelse av skjenkeloven under VM.""";

        System.out.println("=== Per-sentence GIR/FAAR breakdown ===\n");

        List<String> sentences = sentenceSplitter.analyze(new TextInput(article)).sentences();
        System.out.println("--- All sentences ---");
        for (int i = 0; i < sentences.size(); i++) {
            System.out.printf("  [%d] %s%n", i, sentences.get(i));
        }

        // Mímir Kristjánsson
        System.out.println("\n--- Mímir Kristjánsson ---");
        printPerSentence(sentences, "Mímir Kristjánsson", List.of(),
                subjectDetector, sentimentAnalyzer);

        ArticleSentiment mimir = girFaarAnalyzer.analyze(
                new ArticlePersonInput(article, "Mímir Kristjánsson"));
        System.out.printf("  TOTAL -> GIR: %s | FAAR: %s%n%n", mimir.gir(), mimir.faar());

        // Jonas Gahr Støre (alias: statsministeren)
        System.out.println("--- Jonas Gahr Støre (alias: statsministeren) ---");
        printPerSentence(sentences, "Jonas Gahr Støre", List.of("statsministeren"),
                subjectDetector, sentimentAnalyzer);

        ArticleSentiment store = girFaarAnalyzer.analyze(
                new ArticlePersonInput(article, "Jonas Gahr Støre", List.of("statsministeren")));
        System.out.printf("  TOTAL -> GIR: %s | FAAR: %s%n", store.gir(), store.faar());
    }

    private static void printPerSentence(final List<String> sentences,
                                          final String personName,
                                          final List<String> aliases,
                                          final SubjectDetector subjectDetector,
                                          final NorBertSentimentAnalyzer sentimentAnalyzer) throws Exception {
        String lowerName = personName.toLowerCase();
        String[] nameParts = personName.split("\\s+");
        String lastName = nameParts[nameParts.length - 1].toLowerCase();

        for (String sentence : sentences) {
            String lower = sentence.toLowerCase();
            String matchedAs = null;

            if (lower.contains(lowerName)) {
                matchedAs = personName;
            } else if (lower.contains(lastName)) {
                matchedAs = nameParts[nameParts.length - 1];
            } else {
                for (String alias : aliases) {
                    if (lower.contains(alias.toLowerCase())) {
                        matchedAs = alias;
                        break;
                    }
                }
            }

            if (matchedAs == null) continue;

            boolean isSubject = subjectDetector.analyze(
                    new PersonSentenceInput(sentence, matchedAs)).isSubject();
            SentimentScore score = sentimentAnalyzer.analyze(new TextInput(sentence));

            String role = isSubject ? "GIR" : "FAAR";
            String truncated = sentence.length() > 80
                    ? sentence.substring(0, 80) + "..."
                    : sentence;
            System.out.printf("  [%s] %s -> %s%n", role, truncated, score);
        }
    }
}
