package no.politikeriavisen.core.analysis;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.politikeriavisen.dto.ArtikelDTO;
import no.politikeriavisen.dto.DataDTO;
import no.politikeriavisen.dto.Person;
import org.springframework.stereotype.Component;

@Component
public class KildeDataAnalyzer {

    public DataDTO analyzeDataByKilde(final List<Object[]> rawData, final String kilde) {

        if (rawData == null || rawData.isEmpty()) {
            return new DataDTO(0.0, 0, new ArrayList<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(),
                    new HashMap<>(), kilde);
        }

        List<Person> allePersoner = new ArrayList<>();
        List<Integer> aldersListe = new ArrayList<>();
        Map<String, Integer> kjoennRatio = new HashMap<>();
        Map<String, Integer> partiMentions = new HashMap<>();
        int totaltAntallArtikler = 0;

        for (Object[] rad : rawData) {
            String navn = (String) rad[0];
            String normalizedParti = PartiNameNormalizer.normalizePartiName((String) rad[1]);
            Integer alder = (Integer) rad[2];
            String kjoenn = (String) rad[3];
            String valgdistrikt = (String) rad[4];
            String lenkerString = (String) rad[5];
            String scrapedAtString = (String) rad[6];
            String[] alleGirSentiment = splitOrEmpty((String) rad[7]);
            String[] alleGirPositiv = splitOrEmpty((String) rad[8]);
            String[] alleGirNegativ = splitOrEmpty((String) rad[9]);
            String[] alleGirNoytral = splitOrEmpty((String) rad[10]);
            String[] alleFaarSentiment = splitOrEmpty((String) rad[11]);
            String[] alleFaarPositiv = splitOrEmpty((String) rad[12]);
            String[] alleFaarNegativ = splitOrEmpty((String) rad[13]);
            String[] alleFaarNoytral = splitOrEmpty((String) rad[14]);

            if (lenkerString != null && !lenkerString.isEmpty()) {
                String[] alleLenker = lenkerString.split(",");
                String[] alleScrapedAt = splitOrEmpty(scrapedAtString);

                List<ArtikelDTO> artikler = new ArrayList<>();
                for (int i = 0; i < alleLenker.length; i++) {
                    String lenke = alleLenker[i].trim();
                    if ("ALT".equals(kilde) || lenke.contains(kilde)) {
                        ArtikelDTO artikkel = new ArtikelDTO();
                        artikkel.setLenke(lenke);
                        if (i < alleScrapedAt.length) {
                            artikkel.setScraped(LocalDate.parse(alleScrapedAt[i].trim().substring(0, 10)));
                        }
                        artikkel.setGirSentiment(valueAt(alleGirSentiment, i));
                        artikkel.setGirPositivScore(doubleAt(alleGirPositiv, i));
                        artikkel.setGirNoytralScore(doubleAt(alleGirNoytral, i));
                        artikkel.setGirNegativScore(doubleAt(alleGirNegativ, i));
                        artikkel.setFaarSentiment(valueAt(alleFaarSentiment, i));
                        artikkel.setFaarPositivScore(doubleAt(alleFaarPositiv, i));
                        artikkel.setFaarNoytralScore(doubleAt(alleFaarNoytral, i));
                        artikkel.setFaarNegativScore(doubleAt(alleFaarNegativ, i));
                        artikler.add(artikkel);
                    }
                }

                if (!artikler.isEmpty()) {
                    Person person = new Person();
                    person.setNavn(navn);
                    person.setParti(normalizedParti);
                    person.setAlder(alder);
                    person.setKjoenn(kjoenn);
                    person.setValgdistrikt(valgdistrikt);
                    person.setAntallArtikler(artikler.size());
                    person.setLenker(artikler);

                    allePersoner.add(person);
                    partiMentions.merge(normalizedParti, artikler.size(), Integer::sum);
                    if (alder != null) {
                        aldersListe.add(alder);
                    }
                    if (kjoenn != null && !kjoenn.isEmpty()) {
                        kjoennRatio.merge(kjoenn, 1, Integer::sum);
                    }
                    totaltAntallArtikler += artikler.size();
                }
            }
        }

        double gjennomsnittligAlder = aldersListe.isEmpty() ? 0.0
                : aldersListe.stream().mapToInt(Integer::intValue).average().orElse(0.0);

        return new DataDTO(
                gjennomsnittligAlder,
                totaltAntallArtikler,
                new ArrayList<>(allePersoner),
                kjoennRatio,
                beregnKjoennProsent(kjoennRatio),
                partiMentions,
                beregnPartiProsent(partiMentions),
                kilde);
    }

    private String[] splitOrEmpty(final String s) {
        if (s == null || s.isEmpty()) {
            return new String[0];
        }
        return s.split(",", -1);
    }

    private String valueAt(final String[] arr, final int i) {
        if (i < arr.length && !arr[i].isBlank()) {
            return arr[i].trim();
        }
        return null;
    }

    private Double doubleAt(final String[] arr, final int i) {
        if (i < arr.length && !arr[i].isBlank()) {
            try {
                return Double.parseDouble(arr[i].trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    public Map<String, Double> beregnPartiProsent(final Map<String, Integer> partiMentions) {
        if (partiMentions == null || partiMentions.isEmpty()) {
            return new HashMap<>();
        }
        int totalt = partiMentions.values().stream().mapToInt(Integer::intValue).sum();
        if (totalt == 0) {
            return new HashMap<>();
        }
        return partiMentions.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (e.getValue() * 100.0) / totalt));
    }

    public Map<String, Double> beregnKjoennProsent(final Map<String, Integer> kjoennRatio) {
        if (kjoennRatio == null || kjoennRatio.isEmpty()) {
            return new HashMap<>();
        }
        int totalt = kjoennRatio.values().stream().mapToInt(Integer::intValue).sum();
        if (totalt == 0) {
            return new HashMap<>();
        }
        return kjoennRatio.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (e.getValue() * 100.0) / totalt));
    }
}