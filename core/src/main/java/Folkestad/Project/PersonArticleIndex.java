package folkestad.project;

import lombok.Getter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * PersonArticleIndex holder oversikt over hvilke artikler hver person er nevnt
 * i.
 * Gir metoder for å legge til og hente personer og artikler.
 */
public class PersonArticleIndex {
    @Getter
    private final Map<String, Set<String>> index = new HashMap<>();

    /**
     * Legger til en artikkellenke for en person.
     *
     * @param person     Personen som skal knyttes til artikkelen
     * @param articleUrl URL til artikkelen
     */
    public void addMention(final String person, final String articleUrl) {
        index.computeIfAbsent(person, k -> new HashSet<>()).add(articleUrl);
    }

    /**
     * Legger til flere personer for én artikkel.
     *
     * @param persons    Samling av personer som skal knyttes til artikkelen
     * @param articleUrl URL til artikkelen
     */
    public void addMentions(final Collection<String> persons, final String articleUrl) {
        for (String person : persons) {
            addMention(person, articleUrl);
        }
    }

    /**
     * Henter alle artikler en person er nevnt i.
     *
     * @param person Personen som skal søkes etter
     * @return Set med artikkel-URLer hvor personen er nevnt
     */
    public Set<String> getArticlesForPerson(final String person) {
        return index.getOrDefault(person, Collections.emptySet());
    }

    /**
     * Henter alle personer som er registrert i indeksen.
     *
     * @return Set med alle personnavn
     */
    public Set<String> getAllPersons() {
        return index.keySet();
    }
}



