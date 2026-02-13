CREATE TABLE IF NOT EXISTS kandidat_link (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    link VARCHAR(1000) NOT NULL,
    nettsted VARCHAR(50),
    scraped_at DATETIME,
    kandidat_navn VARCHAR(255) NOT NULL,
    FOREIGN KEY (kandidat_navn) REFERENCES kandidat_stortingsvalg(navn) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS kandidat_stortingsvalg (
    navn VARCHAR(255) PRIMARY KEY,
    valg VARCHAR(255),
    valgdistrikt VARCHAR(255),
    partikode VARCHAR(10),
    partinavn VARCHAR(255),
    display_order INT,
    kandidatnr INT,
    bosted VARCHAR(255),
    stilling VARCHAR(255),
    foedselsdato DATE,
    alder INT,
    kjoenn VARCHAR(10),
    INDEX idx_valgdistrikt (valgdistrikt),
    INDEX idx_partikode (partikode)
);

CREATE TABLE IF NOT EXISTS innlegg (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    link VARCHAR(1000) NOT NULL UNIQUE,
    sammendrag TEXT,
    kompresjon_ratio DOUBLE,
    antall_ord_original INT,
    antall_ord_sammendrag INT,
    INDEX idx_link (link),
    INDEX idx_kompresjon_ratio (kompresjon_ratio)
);