insert into etablissement (uai, siren, departement, typeetablissement) values 
('Etab1', 'SIREN', '28', 'CFA'),
('Etab2', 'SIREN', '28', 'LPO'),
('28', 'NULL', 'Centre', 'Total');

DROP TABLE IF EXISTS est_activee;
CREATE TABLE est_activee (
    uid character varying NOT NULL,
    datedebutactivation date NOT NULL,
    datefinactivation date,
    PRIMARY KEY (uid, datedebutactivation)
);
insert into est_activee (uid, datedebutactivation, datefinactivation) values 
('f01', '2012-01-03', null),
('f02', '2012-01-03', '2012-01-05'),
('f03', '2012-01-04', null),
('f04', '2012-01-03', null),
('f05', '2012-02-03', '2012-05-05'),
('f06', '2012-02-04', null),
('f07', '2012-02-03', null),
('f08', '2012-03-03', '2012-04-05'),
('f09', '2012-03-04', null),
('f10', '2012-03-03', null),
('f11', '2012-04-03', '2012-06-05'),
('f12', '2012-04-04', null),
('f13', '2012-05-03', null),
('f14', '2012-06-03', '2012-08-05'),
('f15', '2012-06-04', null),
('f16', '2012-06-03', null),
('f17', '2012-07-03', '2012-09-05'),
('f18', '2012-08-04', null),
('f05', '2012-09-03', '2012-10-05'),
('f11', '2012-10-03', null);

insert into acommeprofil (uid, uai, nomprofil, datedebutprofil, datefinprofil) values 
('f01', 'Etab1', 'Profil1', '2012-01-03', null),
('f02', 'Etab1', 'Profil2', '2012-01-03', '2012-01-05'),
('f03', 'Etab1', 'Profil3', '2012-01-04', null),
('f04', 'Etab1', 'Profil1', '2012-01-03', null),
('f05', 'Etab1', 'Profil2', '2012-02-03', '2012-05-05'),
('f06', 'Etab1', 'Profil3', '2012-02-04', null),
('f07', 'Etab1', 'Profil1', '2012-02-03', null),
('f08', 'Etab1', 'Profil2', '2012-03-03', '2012-04-05'),
('f09', 'Etab1', 'Profil3', '2012-03-04', null),
('f10', 'Etab2', 'Profil1', '2012-03-03', null),
('f11', 'Etab2', 'Profil2', '2012-04-03', '2012-06-05'),
('f12', 'Etab2', 'Profil3', '2012-04-04', null),
('f13', 'Etab3', 'Profil1', '2012-05-03', null),
('f14', 'Etab3', 'Profil2', '2012-06-03', '2012-08-05'),
('f15', 'Etab1', 'Profil3', '2012-06-04', null),
('f16', 'Etab1', 'Profil1', '2012-06-03', null),
('f17', 'Etab1', 'Profil2', '2012-07-03', '2012-09-05'),
('f18', 'Etab1', 'Profil3', '2012-08-04', null),
('f05', 'Etab1', 'Profil2', '2012-09-03', '2012-10-05'),
('f11', 'Etab2', 'Profil2', '2012-10-03', null);