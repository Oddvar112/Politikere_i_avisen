# DataPersonNRK

Et web scraping og analyseprosjekt som automatisk overvåker norske nyhetssider for å identifisere og analysere omtale av stortingskandidater i mediene. det kjøer på cloudfair og en vps og kan ses på siden: https://kvasirsbrygg.no/

## Beskrivelse

DataPersonNRK er et komprehensivt system som samler inn og analyserer medieomtale av norske stortingskandidater fra nyhetssider. 

### Hovedfunksjoner

- **Automatisk web scraping** av NRK, VG, E24 og Dagbladet
- **Intelligent navnegjenkjenning** av stortingskandidater i artikler
- **Statistisk analyse** av medieomtale fordelt på parti, kjønn og alder
- **Tekstsammendrag** av artikler ved hjelp av algoritme
- **REST API** for tilgang til analysedata
- **Sanntids oppdateringer** med planlagte scraping-jobber

## Teknologier

- **Backend**: Java 17, Spring Boot 3.2.3, Spring Data JPA
- **Database**: MySQL 8 med Hibernate ORM
- **Web Scraping**: Jsoup for HTML parsing og RSS feed prosessering
- **Build Tool**: Maven (multi-modul prosjekt)
- **Containerization**: Docker med multi-stage build
- **Scheduling**: Spring Scheduling for automatiserte oppgaver

## Arkitektur

Prosjektet følger en modulær arkitektur med følgende komponenter:

```
dataPersonNRK/
├── model/          # JPA entiteter og repositories
├── core/           # Scraping logikk og navnegjenkjenning
├── dto/            # Data Transfer Objects
├── server/         # REST API og scheduling
└── Dockerfile      # Container konfigurasjon
```

### Kjernemoduler

- **Model**: Inneholder alle database entiteter (Person, KandidatStortingsvalg, Innlegg)
- **Core**: Web scrapers, navnegjenkjenning og tekstanalyse
- **DTO**: Data transfer objekter for API kommunikasjon
- **Server**: REST controller, tjenester og planlagte oppgaver

## Konfigurasjon

### Database Konfigurasjon

Applikasjonen er konfigurert for MySQL i `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://host:3306/database
spring.datasource.username=${MYSQLUSER}
spring.datasource.password=${MYSQLPASSWORD}
spring.jpa.hibernate.ddl-auto=update
```

### Scraping Konfigurasjon

Scraping kjører automatisk hver 3. time med følgende kilder:
- **NRK**: RSS feeds fra alle regioner og kategorier
- **VG**: Frontpage DOM scraping
- **E24**: Kategori-basert scraping
- **Dagbladet**: Frontpage artikkel-lenker

## API Dokumentasjon

### Hent Analysedata

```http
GET /api/analyse/{kilde}
```

**Parametere:**
- `kilde`: `nrk`, `vg`, `e24`, `dagbladet`, eller `alt`
- `fraDato` (valgfri): ISO datetime for startdato
- `tilDato` (valgfri): ISO datetime for sluttdato

**Eksempel:**
# Hent NRK analysedata for januar 2025
curl "https://api.kvasirsbrygg.no/api/analyse/nrk?fraDato=2025-01-01T00:00:00&tilDato=2025-01-31T23:59:59"

# Hent all data fra alle kilder
curl "https://api.kvasirsbrygg.no/api/analyse/alt"

# Hent VG spesifikk data
curl "https://api.kvasirsbrygg.no/api/analyse/vg"

**Response:**
```json
{
  "gjennomsnittligAlder": 45.2,
  "totaltAntallArtikler": 1250,
  "kjoennRatio": {
    "Mann": 42,
    "Kvinne": 23
  },
  "kjoennProsentFordeling": {
    "Mann": 65.4,
    "Kvinne": 34.6
  },
  "partiMentions": {
    "Arbeiderpartiet": 294,
    "Høyre": 248,
    "Fremskrittspartiet": 190,
    "Senterpartiet": 156,
    "Sosialistisk Venstreparti": 142,
    "Miljøpartiet De Grønne": 98,
    "Kristelig Folkeparti": 87,
    "Venstre": 35
  },
  "partiProsentFordeling": {
    "Arbeiderpartiet": 23.5,
    "Høyre": 19.8,
    "Fremskrittspartiet": 15.2,
    "Senterpartiet": 12.5,
    "Sosialistisk Venstreparti": 11.4,
    "Miljøpartiet De Grønne": 7.8,
    "Kristelig Folkeparti": 7.0,
    "Venstre": 2.8
  },
  "allePersonernevnt": [
    {
      "navn": "Jonas Gahr Støre",
      "alder": 64,
      "kjoenn": "Mann",
      "parti": "Arbeiderpartiet",
      "valgdistrikt": "Oslo",
      "antallArtikler": 45,
      "lenker": [
        {
          "lenke": "https://www.nrk.no/norge/stortingsvalg-2025-1.16234567",
          "scraped": "2025-01-15"
        },
        {
          "lenke": "https://www.nrk.no/politikk/nye-tiltak-1.16234568",
          "scraped": "2025-01-14"
        }
      ]
    },
    {
      "navn": "Erna Solberg",
      "alder": 64,
      "kjoenn": "Kvinne",
      "parti": "Høyre",
      "valgdistrikt": "Rogaland",
      "antallArtikler": 38,
      "lenker": [
        {
          "lenke": "https://www.nrk.no/rogaland/hoyre-strategi-1.16234569",
          "scraped": "2025-01-13"
        },
        {
          "lenke": "https://www.nrk.no/politikk/opposisjon-kritikk-1.16234570",
          "scraped": "2025-01-12"
        }
      ]
    },
    {
      "navn": "Sylvi Listhaug",
      "alder": 47,
      "kjoenn": "Kvinne", 
      "parti": "Fremskrittspartiet",
      "valgdistrikt": "Møre og Romsdal",
      "antallArtikler": 29,
      "lenker": [
        {
          "lenke": "https://www.nrk.no/mr/innvandring-debatt-1.16234571",
          "scraped": "2025-01-11"
        }
      ]
    }
  ],
  "kilde": "nrk"
}
```

### Hent Sammendrag

```http
GET /api/analyse/sammendrag?link={artikkel_url}
```

**Response:**
```json
{
  "link": "https://nrk.no/artikkel",
  "sammendrag": "Sammendrag av artikkelen...",
  "kompresjonRatio": 0.25,
  "antallOrdOriginal": 800,
  "antallOrdSammendrag": 200
}
```

## Algoritmer og Teknikker

### Navnegjenkjenning

Systemet bruker en kombinasjon av:
- **Regex-basert ekstrahering** for norske navnemønstre
- **Database-matching** mot kjente stortingskandidater
- **Kontekstfiltrering** for å redusere false positives

### Tekstsammendrag

algoritme basert på:
- **Setningsscoring** ved hjelp av ordoverlapp-matriser
- **Paragrafanalyse** for strukturert sammendrag
- **Kompresjonratio** på cirka 20% av original tekst


## Datamodell

### Hovedentiteter

- **KandidatStortingsvalg**: Informasjon om stortingskandidater
- **KandidatLink**: Lenker mellom kandidater og artikler
- **Innlegg**: Artikkelsammendrag og metadata

### Relasjoner

```
KandidatStortingsvalg 1:N KandidatLink
Person 1:N PersonLink
Innlegg 1:1 ArticleURL
```

## Utvikling

### Code Quality

Prosjektet bruker:
- **Checkstyle** for kodestil
- **SpotBugs** for statisk analyse
- **JaCoCo** for test coverage
