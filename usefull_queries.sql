##### Delete services stats compressed or not since a specific time #####
# Delete all monthly, weekly and services stats since first day of month
delete from seconnectemois where mois >= '2012-12-01';
delete from connexionprofilmois where mois >= '2012-12-01';
delete from seconnectesemaine where premierjoursemaine >= '2012-12-01';
delete from connexionprofilsemaine where semaine >= '2012-12-01';
delete from connexionservicejour where jour >= '2012-12-01';
delete from nombredevisiteurs where jour >= '2012-12-01';

##### Find all services names #####
select distinct(nomservice) from connexionservicejour;
select distinct(truncatedfname) from connexionservicejour;

##### Update services names #####
select count(*) from connexionservicejour where nomservice = 'Listes de diffusions Clg du 37';
update connexionservicejour set nomservice = 'Listes de diffusions' where nomservice = 'Listes de diffusions Clg du 37';