#!/bin/bash

if ( [ $# != 3 ] ) ;
then
        echo
        echo "script pour decouper un fichier de log d'un mois complet en plusieurs fichiers, un par jour dans le mois."
        echo
        echo "usage :    $0 fileNameLogMonth month dest"
        echo
        echo "    fileNameLogMonth: le fichier de log du mois complet"
        echo "          month   : le mois traité ex 2016-11 (doit etre adapté au format des dates dans les logs)" 
        echo "          dest    : répertoire de destination dédié aux fichier de log journalier, ne pas y  mettre autre chose" 
        echo
        exit 1;
fi

repOut=$3
fileIn=$1
fileOut="${repOut}/"`basename $fileIn`
mois=$2
for i in {1..31};
do
        num=`printf "%02d" $i`
        jour=$mois-$num;
        grep "^$jour" $fileIn > $fileOut-$num;
done;

