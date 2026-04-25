package no.politikeriavisen.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtikelDTO {
    private String lenke;
    private LocalDate scraped;
    private String girSentiment;
    private Double girPositivScore;
    private Double girNoytralScore;
    private Double girNegativScore;
    private String faarSentiment;
    private Double faarPositivScore;
    private Double faarNoytralScore;
    private Double faarNegativScore;
}
