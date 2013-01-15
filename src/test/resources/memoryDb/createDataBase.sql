CREATE TABLE acommeprofil (
    uid character varying NOT NULL,
    uai character varying NOT NULL,
    nomprofil character varying NOT NULL,
    datedebutprofil date NOT NULL,
    datefinprofil date,
    PRIMARY KEY (uid, uai, nomprofil, datedebutprofil)
);
CREATE TABLE connexionprofilmois (
    nomprofil character varying NOT NULL,
    uai character varying NOT NULL,
    mois date NOT NULL,
    nbconnexion integer NOT NULL,
    nbpersonne integer,
    moyenneconnexion double precision,
    PRIMARY KEY (nomprofil, uai, mois, nbconnexion)
);
CREATE TABLE connexionprofilsemaine (
    nbconnexion integer NOT NULL,
    semaine date NOT NULL,
    nomprofil character varying NOT NULL,
    uai character varying NOT NULL,
    nbpersonne integer,
    moyenneconnexion double precision,
    PRIMARY KEY (nbconnexion, semaine, nomprofil, uai)
);
CREATE TABLE connexionservicejour (
    uid character varying NOT NULL,
    nomprofil character varying NOT NULL,
    uai character varying NOT NULL,
    nomservice character varying NOT NULL,
    jour date NOT NULL,
    nbconnexionservice integer,
    PRIMARY KEY (uid, nomprofil, uai, nomservice, jour)
);
CREATE TABLE est_activee (
    uid character varying NOT NULL,
    datedebutactivation date NOT NULL,
    datefinactivation date,
    PRIMARY KEY (uid, datedebutactivation)
);

CREATE TABLE etablissement (
    uai character varying NOT NULL,
    siren character varying NOT NULL,
    departement character varying,
    typeetablissement character varying,
    PRIMARY KEY (uai)
);
CREATE TABLE nombredevisiteurs (
    jour date NOT NULL,
    uai character varying NOT NULL,
    nbvisites integer,
    nbvisiteurs integer,
    typeetab character varying NOT NULL,
    typestat character varying NOT NULL,
    PRIMARY KEY (jour, uai, typeetab)
);
CREATE TABLE seconnectemois (
    uai character varying NOT NULL,
    nomprofil character varying NOT NULL,
    uid character varying NOT NULL,
    mois date NOT NULL,
    nbconnexionmois integer,
    moyennemois double precision,
    PRIMARY KEY (uai, nomprofil, uid, mois)
);
CREATE TABLE seconnectesemaine (
    uai character varying NOT NULL,
    nomprofil character varying NOT NULL,
    uid character varying NOT NULL,
    premierjoursemaine date NOT NULL,
    nbconnexionsemaine integer,
    moyennesemaine double precision,
    PRIMARY KEY (uai, nomprofil, uid, premierjoursemaine)
);

