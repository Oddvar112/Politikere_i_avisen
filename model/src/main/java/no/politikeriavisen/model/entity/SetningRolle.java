package no.politikeriavisen.model.entity;

/**
 * Rolle en politiker har i en analysert setning:
 * <ul>
 *   <li>{@link #GIR} — politikeren er grammatisk subjekt og "gir" dermed
 *       sentimentet (f.eks. "Støre kritiserte utspillet").</li>
 *   <li>{@link #FAAR} — politikeren nevnes, men ikke som subjekt, og "mottar"
 *       dermed sentimentet (f.eks. "Støre ble kritisert").</li>
 *   <li>{@link #UKJENT} — SubjectDetector klarte ikke å klassifisere
 *       setningen (f.eks. parse-feil). Setningen lagres likevel for
 *       transparens, men inngår ikke i GIR-/FAAR-aggregatet.</li>
 * </ul>
 */
public enum SetningRolle {
    GIR,
    FAAR,
    UKJENT
}
