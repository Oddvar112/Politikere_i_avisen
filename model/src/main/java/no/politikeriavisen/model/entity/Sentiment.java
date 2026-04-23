package no.politikeriavisen.model.entity;

/**
 * Sentiment-klassifisering for en analysert setning. Speiler verdiene i
 * {@code kvasirsbrygg.news_analyzer.domain.Sentiment} slik at
 * {@code model}-modulen ikke avhenger av {@code analysis}-modulen.
 */
public enum Sentiment {
    POSITIV,
    NEGATIV,
    NOYTRAL
}
