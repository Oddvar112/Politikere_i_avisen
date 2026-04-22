package no.politikeriavisen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonSentimentDTO {
    private String personNavn;
    private String girSentiment;
    private Double girPositivScore;
    private Double girNegativScore;
    private String faarSentiment;
    private Double faarPositivScore;
    private Double faarNegativScore;
}
