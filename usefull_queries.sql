##### Delete services stats compressed or not since a specific time #####
# Delete all monthly, weekly and services stats since first day of month
delete from seconnectemois where mois >= '2013-09-01';
delete from connexionprofilmois where mois >= '2013-09-01';
delete from seconnectesemaine where premierjoursemaine >= '2013-09-01';
delete from connexionprofilsemaine where semaine >= '2013-09-01';
delete from connexionservicejour where jour >= '2013-09-01';
delete from nombredevisiteurs where jour >= '2013-09-01';

##### Find all services names #####
select distinct(nomservice), truncatedfname from connexionservicejour order by nomservice;
select distinct(truncatedfname), nomservice from connexionservicejour order by truncatedfname;

##### Update services names #####
select count(*) from connexionservicejour where nomservice = 'Listes de diffusions Clg du 37';
update connexionservicejour set nomservice = 'Listes de diffusions' where nomservice = 'Listes de diffusions Clg du 37';

##### Strange checks #####
# Lot of connexions to a service by only one people
select uai, uid, nomprofil, nomservice, jour, nbconnexionservice from connexionservicejour where nbconnexionservice > 200;

##### Bad service name : ALREADY SET #####
select distinct(truncatedfname), count(*) from connexionservicejour where nomservice = 'ALREADY SET' group by truncatedfname;

##### Actualisation rentrée 2013 #####
select count(*) from connexionservicejour where nomservice = 'AidesInfosRegion';
update connexionservicejour set nomservice = 'Aides & Informations de la Région pour les CFA' where nomservice = 'AidesInfosRegion';

select count(*) from connexionservicejour where nomservice = 'AnnoncesRECIA';
update connexionservicejour set nomservice = 'Annonces RECIA' where nomservice = 'AnnoncesRECIA';

select count(*) from connexionservicejour where nomservice = 'AtelierTic';
update connexionservicejour set nomservice = 'Atelier TIC' where nomservice = 'AtelierTic';

select count(*) from connexionservicejour where nomservice = 'DocENT';
update connexionservicejour set nomservice = 'Documentation des Admins ENT' where nomservice = 'DocENT';

select count(*) from connexionservicejour where nomservice = 'MoodleWPP';
update connexionservicejour set nomservice = 'Cours en ligne / Espace Moodle' where nomservice = 'MoodleWPP';

select count(*) from connexionservicejour where nomservice = 'Sconet_absences';
update connexionservicejour set nomservice = 'Sconet Absences' where nomservice = 'Sconet_absences';
select count(*) from connexionservicejour where nomservice = 'SconetAbsences';
update connexionservicejour set nomservice = 'Sconet Absences' where nomservice = 'SconetAbsences';

select count(*) from connexionservicejour where nomservice = 'Sconet_notes';
update connexionservicejour set nomservice = 'Sconet Notes' where nomservice = 'Sconet_notes';
select count(*) from connexionservicejour where nomservice = 'SconetNotes';
update connexionservicejour set nomservice = 'Sconet Notes' where nomservice = 'SconetNotes';

select count(*) from connexionservicejour where truncatedfname = 'esup-filemanager' and nomservice != 'Mes espaces de stockage';
update connexionservicejour set nomservice = 'Mes espaces de stockage' where truncatedfname = 'esup-filemanager' and nomservice != 'Mes espaces de stockage';

select count(*) from connexionservicejour where truncatedfname = 'ESCO-Indicators' and nomservice != 'Indicateurs d''usages';
update connexionservicejour set nomservice = 'Indicateurs d''usages' where truncatedfname = 'ESCO-Indicators' and nomservice != 'Indicateurs d''usages';
select count(*) from connexionservicejour where truncatedfname = 'Statistiques' and nomservice != 'Indicateurs d''usages';
update connexionservicejour set nomservice = 'Indicateurs d''usages' where truncatedfname = 'Statistiques' and nomservice != 'Indicateurs d''usages';

select count(*) from connexionservicejour where (truncatedfname = 'EducHorus' or truncatedfname = 'Pronote' or truncatedfname = 'Scolarite.net' or truncatedfname = 'VieScolaire') and nomservice != 'VieScolaire';
update connexionservicejour set nomservice = 'VieScolaire' where (truncatedfname = 'EducHorus' or truncatedfname = 'Pronote' or truncatedfname = 'Scolarite.net' or truncatedfname = 'VieScolaire') and nomservice != 'VieScolaire';

select count(*) from connexionservicejour where nomservice = 'Mon Compte Ent';
select count(*) from connexionservicejour where truncatedfname = 'ESCO-MCE' and nomservice != 'Mon Compte Ent';
update connexionservicejour set nomservice = 'Mon compte ENT' where truncatedfname = 'ESCO-MCE' and nomservice != 'Mon compte ENT';

update connexionservicejour set nomservice = 'Gestion des comptes' where truncatedfname = 'Sarapis' and nomservice != 'Gestion des comptes';

delete from seconnectemois where mois >= '2013-09-01';
delete from connexionprofilmois where mois >= '2013-09-01';
delete from seconnectesemaine where premierjoursemaine >= '2013-09-01';
delete from connexionprofilsemaine where semaine >= '2013-09-01';
delete from connexionservicejour where jour >= '2013-09-01';
delete from nombredevisiteurs where jour >= '2013-09-01';

select nomservice, count(*) from connexionservicejour where (nomservice = 'Sconet Absences' or nomservice = 'Sconet Notes' or nomservice = 'Mes espaces de stockage' 
	or nomservice = 'Indicateurs d''usages' or nomservice = 'VieScolaire' or nomservice = 'Mon Compte Ent' or nomservice = 'Cours en ligne / Espace Moodle' 
	or nomservice = 'Atelier TIC' or nomservice = 'Aides & Informations de la Région pour les CFA') and jour >= '2013-09-01' group by nomservice order by nomservice;

