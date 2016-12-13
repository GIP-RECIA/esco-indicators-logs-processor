#!/bin/bash

if ( [ $# != 2 ] ) ;
then
        echo
        echo "Lance indicators-backend pour chaque jour du mois contenue dans le répertoire donné en paramètre"
        echo "Les logs du  répertoire doivent être  creer par splitMois.sh "
        echo
        echo "usage $0 splitMonthDirectory month"
        echo " splitMonthDirectory : répertoire contenant un fichier par jour se terminant par le numero du jour dans le mois (01..31)" 
        echo " month : le mois des logs a traiter"
        echo
        exit 1
fi
date

# Pour la compression manuelle, utiliser :
# java -jar indicators-backend.jar [WEEK | MONTH] [Premier jour periode]

# Configuration
MAIL_RECIPENT=ent@recia.fr
ERROR_LOG_FILE="/home/esco/logs/indicators_backend_errors.log"
OUTPUT_LOG_FILE="/home/esco/logs/indicators_backend.log"
gcLogFile="/home/esco/logs/indicators_backend.gc"

SCRIPT_DIR=$( cd "$( dirname "$0" )" && pwd )
BATCH_DIR=$SCRIPT_DIR
BATCH_JAR="indicators-backend.jar"
LOG4J_FILE="$BATCH_DIR/conf/log4j.xml"


# Java configuration
# si modif de la taille memoire modifier le test en debut de script
VM_ARGS="-Xmx6144m -XX:+UseG1GC -Xloggc:${gcLogFile} -XX:+PrintGCDateStamps -XX:+PrintGCDetails"
SYS_PROPS="-Dfile.encoding=UTF-8 -Duser.language=fr -Duser.contry=FR -Dlog4j.configuration=file:$LOG4J_FILE"


# Choix du repertoire  mensuel de stats

repIn="$1"
mois="$2"


# Context dir
cd $BATCH_DIR

# Run batch in default mode : OFF
for i in {1..31};
do
        num=`printf "%02d" $i`
        jour=$mois-$num;
        STATS_FILE="$repIn/*$jour"
        if [ -s $STATS_FILE ]
        then
                echo "java $VM_ARGS $SYS_PROPS -jar $BATCH_JAR OFF $STATS_FILE";
                java $VM_ARGS $SYS_PROPS -jar $BATCH_JAR OFF $STATS_FILE 2> $ERROR_LOG_FILE | tee $OUTPUT_LOG_FILE
        fi
        #EXIT_STATUS=$?
done;
