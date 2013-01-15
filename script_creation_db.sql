CREATE TABLE etablissement (
    uai varchar(16) NOT NULL,
    siren varchar(32) NOT NULL,
    departement varchar(16),
    typeetablissement varchar(16),
    PRIMARY KEY (uai)
);
CREATE TABLE acommeprofil (
    uid varchar(16) NOT NULL,
    uai varchar(16) NOT NULL,
    nomprofil varchar(32) NOT NULL,
    datedebutprofil date NOT NULL,
    datefinprofil date,
    PRIMARY KEY (nomprofil, uid, uai, datedebutprofil)
);
CREATE TABLE connexionprofilmois (
    nomprofil varchar(32) NOT NULL,
    uai varchar(16) references etablissement(uai),
    mois date NOT NULL,
    nbconnexion integer NOT NULL,
    nbpersonne integer,
    moyenneconnexion double precision,
    PRIMARY KEY (nomprofil, uai, mois, nbconnexion)
);
CREATE TABLE connexionprofilsemaine (
    nbconnexion integer NOT NULL,
    semaine date NOT NULL,
    nomprofil varchar(32) NOT NULL,
    uai varchar(16) NOT NULL,
    nbpersonne integer,
    moyenneconnexion double precision,
    PRIMARY KEY (nbconnexion, semaine, nomprofil, uai)
);
CREATE TABLE connexionservicejour (
    uid varchar(16) NOT NULL,
   	nomprofil varchar(32) NOT NULL,
    uai varchar(16) NOT NULL,
    truncatedfname varchar(64) NOT NULL,
    nomservice varchar(64) NOT NULL,
    jour date NOT NULL,
    nbconnexionservice integer,
    PRIMARY KEY (uid, nomprofil, uai, nomservice, jour),
    FOREIGN KEY (uai) REFERENCES etablissement(uai)
);
CREATE TABLE est_activee (
    uid varchar(16) NOT NULL,
    datedebutactivation date NOT NULL,
    datefinactivation date,
    PRIMARY KEY (uid, datedebutactivation)
);
CREATE TABLE nombredevisiteurs (
    jour date NOT NULL,
    uai varchar(16) NOT NULL,
    nbvisites integer,
    nbvisiteurs integer,
    typeetab varchar(16) NOT NULL,
    typestat varchar(64) NOT NULL,
    PRIMARY KEY (jour, uai, typeetab)
);
CREATE TABLE seconnectemois (
    uai varchar(16) NOT NULL,
    nomprofil varchar(32) NOT NULL,
    uid varchar(16) NOT NULL,
    mois date NOT NULL,
    nbconnexionmois integer,
    moyennemois double precision,
    PRIMARY KEY (uai, nomprofil, uid, mois),
    FOREIGN KEY (uai) REFERENCES etablissement(uai)
);
CREATE TABLE seconnectesemaine (
    uai varchar(16) NOT NULL,
    nomprofil varchar(32) NOT NULL,
    uid varchar(16) NOT NULL,
    premierjoursemaine date NOT NULL,
    nbconnexionsemaine integer,
    moyennesemaine double precision,
    PRIMARY KEY (uai, nomprofil, uid, premierjoursemaine),
    FOREIGN KEY (uai) REFERENCES etablissement(uai)
);
CREATE TABLE configuration (
    cle varchar(32) NOT NULL,
    valeur varchar(64) NOT NULL,
    PRIMARY KEY (cle)
);

INSERT INTO etablissement (uai, siren, departement, typeetablissement) values 
('18', 'Cher', 'Centre', 'Total'),
('28', 'Eure-et-Loir', 'Centre', 'Total'),
('36', 'Indre', 'Centre', 'Total'),
('37', 'Indre-et-Loire', 'Centre', 'Total'),
('41', 'Loir-et-Cher', 'Centre', 'Total'),
('45', 'Loiret', 'Centre', 'Total');